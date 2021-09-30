package org.example.app.handler;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.example.app.dto.GenerateResetCodeRequestDto;
import org.example.app.dto.LoginRequestDto;
import org.example.app.dto.RegistrationRequestDto;
import org.example.app.dto.ResetRequestDto;
import org.example.app.service.UserService;

import java.io.IOException;
import java.util.logging.Level;

@Log
@RequiredArgsConstructor
public class UserHandler {
    private final UserService service;
    private final Gson gson;

    public void register(HttpServletRequest req, HttpServletResponse resp) {
        try {
            log.log(Level.INFO, "register");
            final var requestDto = gson.fromJson(req.getReader(), RegistrationRequestDto.class);
            final var responseDto = service.register(requestDto);
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(responseDto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void login(HttpServletRequest req, HttpServletResponse resp) {
        try {
            log.log(Level.INFO, "register");
            final var requestDto = gson.fromJson(req.getReader(), LoginRequestDto.class);
            final var responseDto = service.login(requestDto);
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(responseDto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getCode(HttpServletRequest req, HttpServletResponse resp) {
        try {
            log.log(Level.INFO, "getResetCode");
            final var requestDto = gson.fromJson(req.getReader(), GenerateResetCodeRequestDto.class);
            final var responseDto = service.generateResetCode(requestDto);
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(responseDto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetPass(HttpServletRequest req, HttpServletResponse resp) {
        try {
            log.log(Level.INFO, "resetPass");
            final var requestDto = gson.fromJson(req.getReader(), ResetRequestDto.class);
            final var responseDto = service.resetPass(requestDto);
            resp.setHeader("Content-Type", "application/json");
            resp.getWriter().write(gson.toJson(responseDto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
