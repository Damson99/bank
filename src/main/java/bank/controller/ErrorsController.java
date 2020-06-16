package bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@RestController
public class ErrorsController implements ErrorController
{
    @Autowired
    private MessageSource messageSource;

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request)
    {
        ModelAndView modelAndView = new ModelAndView();
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if(status != null)
        {
            int statusCode = Integer.valueOf(status.toString());

            if(statusCode == HttpStatus.NOT_FOUND.value())
            {
                modelAndView.addObject("error", 400);
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value())
            {
                modelAndView.addObject("error", 500);
            }
            else
            {
                modelAndView.addObject("error", "");
            }
        }

        modelAndView.addObject("pageTitle", messageSource.getMessage("error.page", null, request.getLocale()));
        modelAndView.setViewName("error");
        return modelAndView;
    }

    @Override
    public String getErrorPath()
    {
        return  "/error";
    }
}
