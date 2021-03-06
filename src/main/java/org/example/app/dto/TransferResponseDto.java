package org.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransferResponseDto {
    private long senderCardId;
    private String senderCardNumber;
    private long recipientCardId;
    private String recipientCardNumber;
}
