package com.Smtd.GestionPerteDoc.security;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.Smtd.GestionPerteDoc.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SystemStateChecker {

    private final UtilisateurRepository utilisateurRepository;
    private boolean systemInitialized = false;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        systemInitialized = utilisateurRepository.count() > 0;
    }

    public boolean isInitialized() {
        return systemInitialized;
    }

    public void refresh() {
        systemInitialized = utilisateurRepository.count() > 0;
    }
}
