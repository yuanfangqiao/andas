set(COMPONENT_SRCS "app_main.c" "app_camera.c" "app_httpd.c" "app_mdns.c" "app_wifi.c" "app_sd.c" "app_uart.c" "app_smart_wifi.c" "app_board.c")
set(COMPONENT_ADD_INCLUDEDIRS "include")

set(COMPONENT_REQUIRES
    esp32-camera
    esp-face
    nvs_flash
    esp_http_server
    fb_gfx
    mdns
    wpa_supplicant
    fatfs
    wear_levelling
    sdmmc
    protocol_examples_common
    esp_websocket_client
    )


set(COMPONENT_EMBED_FILES
        "www/index_ov2640.html.gz"
        "www/index_ov3660.html.gz"
        "www/index_ov5640.html.gz"
        "www/monitor.html.gz")

register_component()
