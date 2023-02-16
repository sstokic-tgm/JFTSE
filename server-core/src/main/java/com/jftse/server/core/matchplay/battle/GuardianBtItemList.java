package com.jftse.server.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GuardianBtItemList {
    private int btItemId;
    private List<GuardianBtItem> guardianBtItems;
}
