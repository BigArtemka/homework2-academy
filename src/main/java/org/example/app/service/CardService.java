package org.example.app.service;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.Card;
import org.example.app.domain.User;
import org.example.app.dto.TransferRequestDto;
import org.example.app.dto.TransferResponseDto;
import org.example.app.exception.*;
import org.example.app.repository.CardRepository;

import java.util.Random;
import java.util.Set;

@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final Random rn = new Random();

    public boolean isNotOwnerByUserId(User user, long ownerId) {
        return ownerId != user.getId();
    }

    public boolean isNotOwnerByCardId(User user, long cardId) {
        return user.getId() != cardRepository.getOwnerById(cardId).orElseThrow(CardNotFoundException::new);
    }

    public Set<Card> getAllByOwnerId(long ownerId) {
        return cardRepository.getAllByOwnerId(ownerId);
    }

    public Card getById(long id) {
        return cardRepository.getById(id).orElseThrow(CardNotFoundException::new);
    }

    public Card createCard(long userId) {
        String number = "**** *" + rn.nextInt(10) + rn.nextInt(10) + rn.nextInt(10);
        return cardRepository.save(userId, number).orElseThrow(CardOrderException::new);
    }

    public Card deleteById(long cardId) {
        return cardRepository.delete(cardId).orElseThrow(CardNotFoundException::new);
    }

    public TransferResponseDto transfer(long from, TransferRequestDto transferRequestDto) {
        final var to = transferRequestDto.getTo();
        final var amount = transferRequestDto.getAmount();
        final var cardFrom = cardRepository.getById(from).orElseThrow(CardNotFoundException::new);
        final var cardTo = cardRepository.getById(to).orElseThrow(CardNotFoundException::new);
        if (amount <= 0) throw new IncorrectAmountException();
        if (amount > cardFrom.getBalance()) throw new InsufficientFundsException();
        cardRepository.transfer(from, to, amount);
        return new TransferResponseDto(cardFrom.getId(), cardFrom.getNumber(), cardTo.getId(), cardTo.getNumber());
    }
}
