package com.ziyad.courselens.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class AskRequest {

    private String question;
    private List<String> conversationHistory;

}
