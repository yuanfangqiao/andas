/* ESPRESSIF MIT License
 * 
 * Copyright (c) 2018 <ESPRESSIF SYSTEMS (SHANGHAI) PTE LTD>
 * 
 * Permission is hereby granted for use on all ESPRESSIF SYSTEMS products, in which case,
 * it is free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


#include "sdkconfig.h"



#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/semphr.h"
#include "freertos/event_groups.h"
#include "freertos/queue.h"

#include "esp_system.h"
#include "esp_log.h"
#include "esp_websocket_client.h"
#include "esp_event.h"
#include "esp_camera.h"
#include "esp_log.h"

#include "nvs_flash.h"
#include "driver/gpio.h"


#include "app_httpd.h"
#include "app_mdns.h"
#include "app_board.h"
#include "app_action.h"

#define NO_DATA_TIMEOUT_SEC 180

static const char *TAG = "WEBSOCKET";

static TimerHandle_t shutdown_signal_timer;
static SemaphoreHandle_t shutdown_sema;

static esp_websocket_client_handle_t client;

/**
 * @brief switch of stream
 * 0 - off
 * 1 - on
 */
static int esp_cmaera_stream_switch = 0;

/**
 * @brief status of esp websocket connect
 * 0 - disconnect
 * 1 - connect
 */
static int esp_websocket_connect_status = 0;


/* Use project configuration menu (idf.py menuconfig) to choose the GPIO to blink,
   or you can edit the following line and set a number here.
*/
//#define BLINK_GPIO CONFIG_BLINK_GPIO
#define BLINK_GPIO 4


static void configure_led(void)
{
    ESP_LOGI(TAG, "Example configured to blink GPIO LED!");
    gpio_reset_pin(BLINK_GPIO);
    /* Set the GPIO as a push/pull output */
    gpio_set_direction(BLINK_GPIO, GPIO_MODE_OUTPUT);
}

static void set_led(uint8_t led_state)
{
    /* Set the GPIO level according to the state (LOW or HIGH)*/
    gpio_set_level(BLINK_GPIO, led_state);
}


static void shutdown_signaler(TimerHandle_t xTimer)
{
    ESP_LOGI(TAG, "No data received for %d seconds, signaling shutdown", NO_DATA_TIMEOUT_SEC);
    xSemaphoreGive(shutdown_sema);
}



#if CONFIG_WEBSOCKET_URI_FROM_STDIN
static void get_string(char *line, size_t size)
{
    int count = 0;
    while (count < size) {
        int c = fgetc(stdin);
        if (c == '\n') {
            line[count] = '\0';
            break;
        } else if (c > 0 && c < 127) {
            line[count] = c;
            ++count;
        }
        vTaskDelay(10 / portTICK_PERIOD_MS);
    }
}
#endif /* CONFIG_WEBSOCKET_URI_FROM_STDIN */

 
/**
 * @brief default cam taks 
 * is success to use
 * @param arg 
 */
static void cam_task(void *arg){
    while (1){
        if(esp_cmaera_stream_switch){
            
            // start get camera steam frame

            int64_t before = esp_timer_get_time();
            //uint8_t *cam_buf = NULL;

            ESP_LOGI(TAG, "taking buffer...");
            // size_t len = cam_take(&cam_buf);
            camera_fb_t *fb = NULL;
            fb = esp_camera_fb_get();
            if(NULL == fb)
            {
                ESP_LOGI(TAG, "Camera capture failed");
            }
            else
            {
                ESP_LOGI(TAG, "size: %d", fb->len);
                esp_websocket_client_send_bin(client, (const char*)fb->buf, fb->len, portMAX_DELAY);
                esp_camera_fb_return(fb);
                fb = NULL;
            }
            int64_t after = esp_timer_get_time();
            ESP_LOGI(TAG, "Sent, time: %lld", after-before);
            vTaskDelay(1); // prevent WDT reset
            /*!< Use a logic analyzer to observe the frame rate */
        }
    }
}

static void app_action(esp_websocket_event_data_t *data){
    ESP_LOGW(TAG, "app_action=%.*s", data->data_len, (char *)data->data_ptr);
    //ESP_LOGI(TAG, "app_action=%s", msg);
    char variable[32];
    strncpy(variable,(char *)data->data_ptr, data->data_len);
    if (!strcmp(variable, "OpenCamera")) {
        ESP_LOGI(TAG,"app action: open camera");
        esp_cmaera_stream_switch = 1;
    }
    if (!strcmp(variable, ACTION_CLOSE_CAMERA)) {
        ESP_LOGI(TAG,"app action: close camera");
        esp_cmaera_stream_switch = 0;
    }
    if (!strcmp(variable, ACTION_LED_ON)) {
        ESP_LOGI(TAG,"app action: led on");
        set_led(1);
    }
    if (!strcmp(variable, ACTION_LED_OFF)) {
        ESP_LOGI(TAG,"app action: led off");
        set_led(0);
    }
}


static void websocket_event_handler(void *handler_args, esp_event_base_t base, int32_t event_id, void *event_data)
{
    esp_websocket_event_data_t *data = (esp_websocket_event_data_t *)event_data;
    switch (event_id) {
    case WEBSOCKET_EVENT_CONNECTED:
        ESP_LOGI(TAG, "WEBSOCKET_EVENT_CONNECTED");
        char *mydata = "hello";
        ESP_LOGI(TAG, "Sending %s", mydata);
        esp_websocket_client_send_text(client, mydata, sizeof(mydata), portMAX_DELAY);
        
        esp_websocket_connect_status = 1;
        xTaskCreate(cam_task, "cam_task", 8192, NULL, configMAX_PRIORITIES, NULL);
        break;
    case WEBSOCKET_EVENT_DISCONNECTED:
        ESP_LOGI(TAG, "WEBSOCKET_EVENT_DISCONNECTED");
        esp_websocket_connect_status = 0;
        break;
    case WEBSOCKET_EVENT_DATA:
        ESP_LOGI(TAG, "WEBSOCKET_EVENT_DATA");
        ESP_LOGI(TAG, "Received opcode=%d", data->op_code);
        if (data->op_code == 0x08 && data->data_len == 2) {
            ESP_LOGW(TAG, "Received closed message with code=%d", 256*data->data_ptr[0] + data->data_ptr[1]);
        } else {
            ESP_LOGW(TAG, "Received=%.*s", data->data_len, (char *)data->data_ptr);
            app_action(data);
        }
        ESP_LOGW(TAG, "Total payload length=%d, data_len=%d, current payload offset=%d\r\n", data->payload_len, data->data_len, data->payload_offset);

        xTimerReset(shutdown_signal_timer, portMAX_DELAY);
        break;
    case WEBSOCKET_EVENT_ERROR:
        ESP_LOGI(TAG, "WEBSOCKET_EVENT_ERROR");
        break;
    }
}




static void websocket_app_start(void)
{
    esp_websocket_client_config_t websocket_cfg = {};

    shutdown_signal_timer = xTimerCreate("Websocket shutdown timer", NO_DATA_TIMEOUT_SEC * 1000 / portTICK_PERIOD_MS,
                                         pdFALSE, NULL, shutdown_signaler);
    shutdown_sema = xSemaphoreCreateBinary();

#if CONFIG_WEBSOCKET_URI_FROM_STDIN
    char line[128];

    ESP_LOGI(TAG, "Please enter uri of websocket endpoint");
    get_string(line, sizeof(line));

    websocket_cfg.uri = line;
    ESP_LOGI(TAG, "Endpoint uri: %s\n", line);

#else
    websocket_cfg.uri = CONFIG_WEBSOCKET_URI;

#endif /* CONFIG_WEBSOCKET_URI_FROM_STDIN */

    ESP_LOGI(TAG, "Connecting to %s...", websocket_cfg.uri);

    client = esp_websocket_client_init(&websocket_cfg);
    esp_websocket_register_events(client, WEBSOCKET_EVENT_ANY, websocket_event_handler, (void *)client);

    esp_websocket_client_start(client);

    /* not stop
    xTimerStart(shutdown_signal_timer, portMAX_DELAY);
    char data[32];
    int i = 0;
    while (i < 5) {
        if (esp_websocket_client_is_connected(client)) {
            int len = sprintf(data, "hello %04d", i++);
            ESP_LOGI(TAG, "Sending %s", data);
            esp_websocket_client_send_text(client, data, len, portMAX_DELAY);
        }
        vTaskDelay(1000 / portTICK_RATE_MS);
    }

    xSemaphoreTake(shutdown_sema, portMAX_DELAY);
    esp_websocket_client_close(client, portMAX_DELAY);
    ESP_LOGI(TAG, "Websocket Stopped");
    esp_websocket_client_destroy(client);
    */
}


void app_main()
{
    ESP_LOGI(TAG, "[APP] Startup..");
    ESP_LOGI(TAG, "[APP] Free memory: %d bytes", esp_get_free_heap_size());
    ESP_LOGI(TAG, "[APP] IDF version: %s", esp_get_idf_version());
    esp_log_level_set("*", ESP_LOG_INFO);
    esp_log_level_set("WEBSOCKET_CLIENT", ESP_LOG_DEBUG);
    esp_log_level_set("TRANSPORT_WS", ESP_LOG_DEBUG);
    esp_log_level_set("TRANS_TCP", ESP_LOG_DEBUG);

    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND)
    {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    
    ESP_ERROR_CHECK(ret);

    ESP_ERROR_CHECK(esp_netif_init());

    ESP_ERROR_CHECK(esp_event_loop_create_default());
    
    app_board_main();

    app_mdns_main();

    websocket_app_start();
    
    /* Configure the peripheral according to the LED type */
    configure_led();

    ESP_LOGI("esp-cam Version",CONFIG_ESP_CAM_VERSION);

    ESP_LOGI("esp-cam up 3",CONFIG_ESP_CAM_VERSION);
}
