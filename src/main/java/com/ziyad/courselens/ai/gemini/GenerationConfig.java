package com.ziyad.courselens.ai.gemini;


import java.util.Map;

public record GenerationConfig(String responseMimeType, Double temperature
        , ThinkingConfig thinkingConfig
        , Map<String, Object> responseSchema) {
}
