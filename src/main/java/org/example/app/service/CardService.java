package org.example.app.service;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.Card;
import org.example.app.dto.TransferRequestDto;
import org.example.app.dto.TransferResponseDto;
import org.example.app.exception.CardNotFoundException;
import org.example.app.exception.CardOrderException;
import org.example.app.exception.TransferException;
import org.example.app.repository.CardRepository;

import java.util.Random;
import java.util.Set;

@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final Random rn = new Random();

    public Set<Card> getAllByOwnerId(long ownerId) {
        return cardRepository.getAllByOwnerId(ownerId);
    }

    public Card getById(long id) {
        return cardRepository.getById(id).orElseThrow(CardNotFoundException::new);
    }

    public Long getOwnerById(long id) {
        return cardRepository.getOwnerById(id).orElseThrow(CardNotFoundException::new);
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
        if (amount <= 0 || amount > cardFrom.getBalance()) throw new TransferException();
        cardRepository.transfer(from, to, amount);
        return new TransferResponseDto(cardFrom.getId(), cardFrom.getNumber(), cardTo.getId(), cardTo.getNumber());
    }
}
