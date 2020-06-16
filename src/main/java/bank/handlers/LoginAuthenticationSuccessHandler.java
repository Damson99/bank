package bank.handlers;

import bank.model.User;
import bank.service.DeviceMetadataService;
import bank.service.UserService;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

@Component
public class LoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler
{
    @Autowired
    private DeviceMetadataService deviceMetadataService;

    @Autowired
    private UserService userService;

    private Log logger = LogFactory.getLog(this.getClass());

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();


    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
            final Authentication authentication) throws IOException
    {
        handle(request, response, authentication);
        loginNotification(authentication, request);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException
    {
        String targetUrl = determinateTargetUrl(authentication);

        if(response.isCommitted())
        {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
        }

        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    private void loginNotification(Authentication authentication, HttpServletRequest request)
    {
        User user = null;
        try
        {
            if(authentication.getPrincipal() != null)
            {
                user = userService.findByUserPersonalIdNumber(authentication.getName());
                deviceMetadataService.verifyDevice(user, request);
            }
        }
        catch (Exception e)
        {
            deviceMetadataService.sendLogin(user, request.getLocale());
        }
    }

    private String determinateTargetUrl(Authentication authentication)
    {
        boolean isAdmin = false;
        boolean isEmployee = false;
        boolean isUser = false;

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for(GrantedAuthority grantedAuthority : authorities)
        {
            switch (grantedAuthority.getAuthority())
            {
                case "ADMIN":
                    isAdmin = true;
                    break;
                case "EMPLOYEE":
                    isEmployee = true;
                    break;
                case "USER":
                    isUser = true;
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        if(isAdmin)
        {
            return "/console";
        }
        else if(isEmployee)
        {
            return "/users";
        }
        else if(isUser)
        {
            return "/transfer";
        }
        else
        {
            throw  new IllegalStateException();
        }
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request)
    {
        HttpSession httpSession = request.getSession(false);
        if(httpSession == null)
            return;
        httpSession.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

    public void setRedirectStrategy(RedirectStrategy redirectStrategy)
    {
        this.redirectStrategy = redirectStrategy;
    }

    protected RedirectStrategy getRedirectStrategy()
    {
        return redirectStrategy;
    }
}
