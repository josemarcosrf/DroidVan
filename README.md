# DroidVan
This repo contains my experimental Android app to control lights, fans and other elements of my 
campervan using a RaspberryPi.

The raspberryPi side can be found in the [RaspVan repo](https://github.com/jmrf/RaspVan).


## Run
The app sends http requests to a server running on the RaspberryPi to switch on/off the lights,
setup light timers and other similar functionality. 

For development purposes is handy to have a mock server where to send requests and receive 
fake data, to run it:
```bash
    python mock_env/mock_RaspVan_server.py <port>
```

After starting the server, the _RaspberryPi IP_ has to be changed accordingly in the app
through the menu on the top right corner.






  

