# Websocket

Websocket requests are supported from the 2025 version.
The implementation is based on OkHttp.

``` javascript
let ws = Websocket("wss://echo.websocket.org", {"User-Agent": "TTS Server"})
ws.on('close', function(code, reason){ // Int, String
    
})

ws.on('error', function(err, response){ // String, NativeResponse
    
})

ws.on('binary', function(buf){ // Buffer
    
})

ws.on('text', function(msg){ // String
    
})

ws.on('open', function(){
    ws.send("Hello world!")
})

// Close
// ws.close(1007, "close~~")

// Force disconnect
// ws.cancel()

```

## Use with plugin TTS getAudioV2

```javascript
var ws = null
let PluginJS = {
    // ...
    
    "onStop": function () {
        if (ws != null) {
            ws.cancel()
        }
    },

    "getAudioV2": function (request, callback) {
        // request is JSON
        // request.text / voice / locale / rate / volume / pitch
        // e.g. request.voice 
                
    
        callback.write(bytes)   // Write a byte array
        callback.close()        // Call after all bytes have been written
        callback.error(string)  // Call when an error occurs
    }
    
}
// ...
    

```
