package bank.service;

import bank.model.DeviceMetadata;
import bank.model.EmailBody;
import bank.model.SenderEvent;
import bank.model.User;
import bank.repository.DeviceMetadataRepository;
import bank.utils.ActualDate;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


import static java.util.Objects.nonNull;

@Service
public class DeviceMetadataService
{
    @Autowired
    private DeviceMetadataRepository deviceMetadataRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ActualDate actualDate;

    private DatabaseReader databaseReader;

    private Parser parser;


    public void verifyDevice(User user, HttpServletRequest request) throws IOException, GeoIp2Exception
    {
        String UNKNOWN = messageSource.getMessage("unknown", null, request.getLocale());

        String ip = "";
        String deviceDetails = "";
        String location = "";
        try
        {
            ip = extractIp(request);
            deviceDetails = getDeviceDetails(request.getHeader("user-agent"));
            location = getIpLocation(ip);
        }
        catch (NullPointerException ex)
        {
        }

        DeviceMetadata existingMetadata = findExistingDevice(user.getId(), deviceDetails, location);

        if(Objects.isNull(existingMetadata))
        {
            DeviceMetadata deviceMetadata = new DeviceMetadata();
            deviceMetadata.setUserId(user.getId());
            deviceMetadata.setLocation(location);
            deviceMetadata.setDeviceDetails(deviceDetails);
            deviceMetadata.setLastLogged(actualDate.getTime());
            deviceMetadataRepository.save(deviceMetadata);

            if(ip == null || ip.equals(""))
                ip = UNKNOWN;
            if(deviceDetails == null || deviceDetails.equals(""))
                deviceDetails = UNKNOWN;
            if(location == null || location.equals(""))
                location = UNKNOWN;

            unknownDeviceNotification(user, deviceDetails, location, ip, request.getLocale());
        }
        else
        {
            loginNotification(user, request.getLocale());

            existingMetadata.setLastLogged(actualDate.getTime());
            deviceMetadataRepository.save(existingMetadata);
        }

    }

    private String extractIp(HttpServletRequest request)
    {
        String clientIp;
        String clientXForwardedForIp = request.getHeader("x-forwarded-for");
        if(nonNull(clientXForwardedForIp))
        {
            clientIp = clientXForwardedForIp.split(" *, *")[0];
        }
        else
        {
            clientIp = request.getRemoteAddr();
        }

        return clientIp;
    }

    private String getDeviceDetails(String userAgent)
    {
        String deviceDetails = null;
        try
        {
            Parser uaParser = new Parser();
            Client client = uaParser.parse(userAgent);
            if (client != null)
            {
                deviceDetails = client.userAgent.family + " " + client.userAgent.major + "." + client.userAgent.minor + " - "
                        + client.os.family + " " + client.os.major + "." + client.os.minor;
            }
        }
        catch (IOException e)
        {
        }

        return deviceDetails;
    }

    private String getIpLocation(String ip) throws IOException, GeoIp2Exception
    {
        InetAddress ipAddress = InetAddress.getByName(ip);
        CityResponse cityResponse = databaseReader.city(ipAddress);
        if(cityResponse != null && !cityResponse.getCity().getName().equals(""))
        {
            return cityResponse.getCity().getName();
        }
        else
        {
            return "";
        }
    }

    private DeviceMetadata findExistingDevice(Long userId, String deviceDetails, String location)
    {
        List<DeviceMetadata> knownDevices = deviceMetadataRepository.findByUserId(userId);

        for(DeviceMetadata existingDevice : knownDevices)
        {
            if(existingDevice.getDeviceDetails().equals(deviceDetails) && existingDevice.getLocation().equals(location))
            {
                return existingDevice;
            }
        }

        return null;
    }

    private void unknownDeviceNotification(User user, String deviceDetails, String location, String ip, Locale locale)
    {
        String subject = messageSource.getMessage("email.unknown.login.notification.subject", null, locale);
        String message = messageSource.getMessage("email.unknown.login.notification.deviceDetails", null, locale)
                + " " + deviceDetails
                + "\n" + messageSource.getMessage("email.unknown.login.notification.location", null, locale)
                + " " + location
                + "\n" + messageSource.getMessage("email.unknown.login.notification.ip", null, locale)
                + " " + ip;

        EmailBody emailBody = new EmailBody(subject, message, user.getEmail());
        eventPublisher.publishEvent(new SenderEvent(locale, emailBody));
    }

    public void sendLogin(User user, Locale locale)
    {
        loginNotification(user, locale);
    }

    private void loginNotification(User user, Locale locale)
    {
        String subject = messageSource.getMessage("message.login.notification.subject", null, locale);
        String message = messageSource.getMessage("email.login.correct", null, locale);
        EmailBody emailBody = new EmailBody(subject, message, user.getEmail());
        eventPublisher.publishEvent(new SenderEvent(locale, emailBody));
    }

    public List<DeviceMetadata> findByUserId(Long id)
    {
        return deviceMetadataRepository.findByUserId(id);
    }
}
