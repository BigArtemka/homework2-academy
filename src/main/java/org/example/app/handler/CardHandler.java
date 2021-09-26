package org.example.app.handler;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.example.app.dto.TransferRequestDto;
import org.example.app.service.CardService;
import org.example.app.util.AuthHelper;
import org.example.app.util.UserHelper;
import org.example.framework.attribute.RequestAttributes;
import org.example.framework.security.Roles;

import java.io.IOException;
import java.util.regex.Matcher;

@Log
@RequiredArgsConstructor
public class CardHandler { // Servlet -> Controller -> Service (domain) -> domain
    private final CardService service;
    private final Gson gson;

    public void getAll(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final var auth = AuthHelper.getAuth(req);
            final var user = UserHelper.getUser(auth);
            final var userId = Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
                    .group("userId"));
            if (userId != user.getId() && !auth.getAuthorities().contains(Roles.ROLE_ADMIN)) {
                resp.sendError(403);
                return;
            }
            final var data = service.getAllByOwnerId(userId);
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getById(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final var auth = AuthHelper.getAuth(req);
            final var user = UserHelper.getUser(auth);
            final var cardId = Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
                    .group("cardId"));
            if (service.getOwnerById(cardId) != user.getId() && !auth.getAuthorities().contains(Roles.ROLE_ADMIN)) {
                resp.sendError(403);
                return;
            }
            final var data = service.getById(cardId);
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void order(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final var auth = AuthHelper.getAuth(req);
            final var user = UserHelper.getUser(auth);
            final var userId = Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
                    .group("userId"));
            if (userId != user.getId() && !auth.getAuthorities().contains(Roles.ROLE_ADMIN)) {
                resp.sendError(403);
                return;
            }
            final var data = service.createCard(userId);
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void blockById(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final var auth = AuthHelper.getAuth(req);
            final var user = UserHelper.getUser(auth);
            final var cardId = Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
                    .group("cardId"));
            if (service.getOwnerById(cardId) != user.getId() && !auth.getAuthorities().contains(Roles.ROLE_ADMIN)) {
                resp.sendError(403);
                return;
            }
            final var data = service.deleteById(cardId);
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void transfer(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final var auth = AuthHelper.getAuth(req);
            final var user = UserHelper.getUser(auth);
            final var cardId = Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR))
                    .group("cardId"));
            if (service.getOwnerById(cardId) != user.getId()) {
                resp.sendError(403);
                return;
            }
            final var requestDto = gson.fromJson(req.getReader(), TransferRequestDto.class);
            final var responseDto = service.transfer(cardId, requestDto);
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(responseDto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
