
package org.n52.eventing.rest.binding;

import javax.servlet.http.HttpServletRequest;
import org.n52.eventing.rest.subscriptions.InvalidSubscriptionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@ControllerAdvice
public class ExceptionHandlerImpl {

    public static final String DEFAULT_ERROR_VIEW = "error";
    private static final String BACKLINK = "href";

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        return createModelAndView(e, req);
    }

    @ExceptionHandler(value = ResourceNotAvailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView unknownResourceHandler(HttpServletRequest req, Exception e) throws Exception {
        return createModelAndView(e, req);
    }


    @ExceptionHandler(value = InvalidSubscriptionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView invalidSubscriptionHandler(HttpServletRequest req, Exception e) throws Exception {
        return createModelAndView(e, req);
    }


    private ModelAndView createModelAndView(Exception e, HttpServletRequest req) {
        ModelAndView mav = new ModelAndView();
        mav.addObject(DEFAULT_ERROR_VIEW, e.getMessage());
        mav.addObject(BACKLINK, req.getRequestURL());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
    }
}
