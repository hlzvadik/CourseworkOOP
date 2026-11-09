package dev.goltsov.entities.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenMeteoApi extends ApiForRequest {
    public OpenMeteoApi() {
        super("api.open-meteo.com/v1/forecast");
        param("latitude", "60.00");
        param("longitude", "30.38");
        param("current", "temperature_2m,wind_speed_10m,relative_humidity_2m");
    }
}
