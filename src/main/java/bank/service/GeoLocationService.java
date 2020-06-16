package bank.service;

import bank.config.MainConfig;
import bank.model.GeoLocation;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;

@Service
public class GeoLocationService
{
    private DatabaseReader databaseReader;

    private MessageSource messageSource;

    public GeoLocationService() throws IOException
    {
        File database = new File(MainConfig.maxmindDirectory + "GeoLite2-City.mmdb");
        databaseReader = new DatabaseReader.Builder(database).build();
    }

    public GeoLocation getLocation(String ip) throws IOException, GeoIp2Exception
    {
        InetAddress ipAddress = InetAddress.getByName(ip);
        CityResponse response = databaseReader.city(ipAddress);

        String cityName = response.getCity().getName();
        String latitude = response.getLocation().getLatitude().toString();
        String longitude = response.getLocation().getLongitude().toString();

        return new GeoLocation(ip, cityName, latitude, longitude);
    }

}
