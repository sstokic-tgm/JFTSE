package com.jftse.emulator.server.database.model.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ItemPart extends Item {
    private String forPlayer;

    private String part;

    private Boolean enchantElement;
    private Boolean enableParcel;

    private Byte level;
    private Byte strength;
    private Byte stamina;
    private Byte dexterity;
    private Byte willpower;

    private Byte addHp;
    private Byte addQuick;
    private Byte addBuff;

    private Byte smashSpeed;
    private Byte moveSpeed;
    private Byte chargeshotSpeed;
    private Byte lobSpeed;
    private Byte serveSpeed;

    private Byte maxStrength;
    private Byte maxStamina;
    private Byte maxDexterity;
    private Byte maxWillpower;

    private Byte ballSpin;
    private Byte atss;
    private Byte dfss;

    private Byte socket;
    private Byte gauge;
    private Byte gaugeBattle;
}
