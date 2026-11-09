package dev.goltsov.entities.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenMeteoApi extends ApiForRequest {

    public OpenMeteoApi() {
        domain = "api.open-meteo.com/v1/forecast";
        variables.put("latitude", "60.00");
        variables.put("longitude", "30.38");
        variables.put("current", "temperature_2m,wind_speed_10m,relative_humidity_2m");

    }
}
