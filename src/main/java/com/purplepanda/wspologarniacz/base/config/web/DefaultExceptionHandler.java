package com.purplepanda.wspologarniacz.base.config.web;

import com.purplepanda.wspologarniacz.api.model.ErrorDto;
import com.purplepanda.wspologarniacz.group.exception.GroupNotFoundException;
import com.purplepanda.wspologarniacz.group.exception.InvalidAffiliationStateException;
import com.purplepanda.wspologarniacz.group.exception.NotGroupMemberException;
import com.purplepanda.wspologarniacz.ranking.exception.CategoryNotFoundException;
import com.purplepanda.wspologarniacz.ranking.exception.RankingNotFoundException;
import com.purplepanda.wspologarniacz.task.TaskNotFoundException;
import com.purplepanda.wspologarniacz.user.authorization.InvalidResourceStateException;
import com.purplepanda.wspologarniacz.user.authorization.UnauthorizedResourceAccessException;
import com.purplepanda.wspologarniacz.user.exception.IncorrectTokenException;
import com.purplepanda.wspologarniacz.user.exception.RequestNotFoundException;
import com.purplepanda.wspologarniacz.user.exception.UserAlreadyExistsException;
import com.purplepanda.wspologarniacz.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class DefaultExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({UserNotFoundException.class, UsernameNotFoundException.class, RequestNotFoundException.class,
            GroupNotFoundException.class, TaskNotFoundException.class, CategoryNotFoundException.class,
            RankingNotFoundException.class})
    @ResponseBody
    ErrorDto handleResourceNotFound(final HttpServletRequest req, final Exception ex) {
        return new ErrorDto()
                .code(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .uri(req.getRequestURI());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalStateException.class, IncorrectTokenException.class})
    @ResponseBody
    ErrorDto handleBadRequest(final HttpServletRequest req, final Exception ex) {
        return new ErrorDto()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .uri(req.getRequestURI());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({UserAlreadyExistsException.class, InvalidAffiliationStateException.class,
            InvalidResourceStateException.class})
    @ResponseBody
    ErrorDto handleResourceAlreadyExists(final HttpServletRequest req, final Exception ex) {
        return new ErrorDto()
                .code(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .uri(req.getRequestURI());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({NotGroupMemberException.class, UnauthorizedResourceAccessException.class})
    @ResponseBody
    ErrorDto handleNotAuthorizedToModifyResources(final HttpServletRequest req, final Exception ex) {
        return new ErrorDto()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getMessage())
                .uri(req.getRequestURI());
    }
}
