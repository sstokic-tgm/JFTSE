package com.ft.emulator.server.database.model.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GuardianStage {
    private Integer MapId = 0;
    private List<Integer> GuardiansLeft = new ArrayList<>();
    private List<Integer> GuardiansRight = new ArrayList<>();
    private List<Integer> GuardiansMiddle = new ArrayList<>();
    private Boolean IsBossStage = false;
    private Integer BossGuardian = 0;
}
