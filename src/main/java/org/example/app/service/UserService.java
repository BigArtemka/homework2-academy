package org.example.app.service;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.User;
import org.example.app.dto.*;
import org.example.app.exception.*;
import org.example.app.jpa.JpaTransactionTemplate;
import org.example.app.repository.UserRepository;
import org.example.framework.security.*;
import org.example.framework.util.KeyValue;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

@RequiredArgsConstructor
public class UserService implements AuthenticationProvider, AnonymousProvider {
    //Token lifetime = 600 seconds
    private final Double TOKEN_LIFETIME = 600.0;
    private final UserRepository repository;
    private final JpaTransactionTemplate transactionTemplate;
    private final PasswordEncoder passwordEncoder;
    private final StringKeyGenerator keyGenerator;
    private final SecureRandom secureRandom;

    @Override
    public Authentication authenticate(Authentication authentication) {
        final var token = (String) authentication.getPrincipal();

        if (repository.getTokenLifeTime(token).orElseThrow(AuthenticationException::new) > TOKEN_LIFETIME) {
            throw new AuthenticationException();
        }

        // Update token created time
        repository.updateTokenCreated(token);

        return repository.findByToken(token)
                .map(o -> new TokenAuthentication(o, null, repository.getRolesByUserId(o), true))
                .orElseThrow(AuthenticationException::new);
    }

    @Override
    public Authentication authenticateBasic(Authentication authentication) {
        final var saved = repository.getByUsernameWithPassword((String) authentication.getPrincipal())
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches((String) authentication.getCredentials(), saved.getPassword())) {
            // FIXME: Security issue
            throw new PasswordNotMatchesException();
        }

        final var user = new User(saved.getId(), saved.getUsername());
        return new BasicAuthentication(user, null,
                repository.getRolesByUserId(user), true);
    }

    @Override
    public AnonymousAuthentication provide() {
        return new AnonymousAuthentication(new User(
                -1,
                "anonymous"
        ));
    }

    public RegistrationResponseDto register(RegistrationRequestDto requestDto) {
        // TODO login:
        //  case-sensitivity: coursar Coursar
        //  cleaning: "  Coursar   "
        //  allowed symbols: [A-Za-z0-9]{2,60}
        //  mis...: Admin, Support, root, ...
        //  мат: ...
        // FIXME: check for nullability
        final var username = requestDto.getUsername().trim().toLowerCase();
        // TODO password:
        //  min-length: 8
        //  max-length: 64
        //  non-dictionary
        final var password = requestDto.getPassword().trim();
        final var hash = passwordEncoder.encode(password);
        final var token = keyGenerator.generateKey();
        final var saved = repository.save(0, username, hash).orElseThrow(RegistrationException::new);

        repository.saveToken(saved.getId(), token);
        return new RegistrationResponseDto(saved.getId(), saved.getUsername(), token);
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        final var username = requestDto.getUsername().trim().toLowerCase();
        final var password = requestDto.getPassword().trim();

        final var result = transactionTemplate.executeInTransaction((entityManager, transaction) -> {
            final var saved = repository.getByUsernameWithPassword(
                    entityManager,
                    transaction,
                    username
            ).orElseThrow(UserNotFoundException::new);

            // TODO: be careful - slow
            if (!passwordEncoder.matches(password, saved.getPassword())) {
                // FIXME: Security issue
                throw new PasswordNotMatchesException();
            }

            final var token = keyGenerator.generateKey();
            repository.saveToken(saved.getId(), token);
            return new KeyValue<>(token, saved);
        });

        // FIXME: Security issue

        final var token = result.getKey();
        final var saved = result.getValue();
        return new LoginResponseDto(saved.getId(), saved.getUsername(), token);
    }

    public GetResetCodeResponseDto generateResetCode(GetResetCodeRequestDto requestDto) {
        final var username = requestDto.getUsername().trim().toLowerCase();
        final var code = String.format("%d%d%d%d%d%d", secureRandom.nextInt(10),
                secureRandom.nextInt(10), secureRandom.nextInt(10), secureRandom.nextInt(10),
                secureRandom.nextInt(10), secureRandom.nextInt(10));
        repository.generateResetCode(username, code);
        return new GetResetCodeResponseDto(username, code);
    }

    public LoginResponseDto resetPass(ResetRequestDto requestDto) {
        final var username = requestDto.getUsername();
        final var password = requestDto.getPassword();
        final var code = requestDto.getResetCode();
        if (!repository.checkResetCode(username, code)) throw new ResetCodeNotMatchesException();
        final var hash = passwordEncoder.encode(password);
        final var user = repository.resetPass(username, hash).orElseThrow(ResetPasswordException::new);
        final var token = keyGenerator.generateKey();
        repository.saveToken(user.getId(), token);
        return new LoginResponseDto(user.getId(), user.getUsername(), token);
    }
}
