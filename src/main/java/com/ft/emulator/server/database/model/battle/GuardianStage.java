package com.ft.emulator.server.database.model.battle;

import java.util.ArrayList;
import java.util.List;

public class GuardianStage {
    public Integer MapId = 0;
    public List<Integer> GuardiansLeft = new ArrayList<>();
    public List<Integer> GuardiansRight = new ArrayList<>();
    public List<Integer> GuardiansMiddle = new ArrayList<>();
    public Boolean IsBossStage = false;
    public Integer BossGuardian = 0;
}
