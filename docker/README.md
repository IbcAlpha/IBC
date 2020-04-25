Docker
------

To run the docker container, copy and edit config.ini from the resources folder. Set user and password.
When the container boots you should get the 2FA notification. Note that config.ini is copied at the
time the container is *built*, not run.


Assuming you are in the root folder of the project:

```
cp resources/config.ini .
docker-compose up
```



