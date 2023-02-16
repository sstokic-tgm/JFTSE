# Change Log

All notable changes to this project will be documented in this file.

Check [Keep a Changelog](http://keepachangelog.com/) for recommendations on how to structure this file.


### 2020-07-25
#### Server

* Refactored Server.
* Optimized Server.
* Different project structure in whole.
* Using Spirng Boot.

#### Game-related

* Everything for home is now finished. Furniture Count, bonuses, house level working.
-- Bonuses applied to single-play matches.

* Shop fixes:
-- You can buy sets now, multiple items at once can be buyed from now on.
-- You can buy sets now, which adds you a character aswell, those items are transferred to that new character.
-- You can buy chars and create them without restarting the client anymore.
-- If enough housing points, you can buy the house with a greater level aswell and have that way more space. (and level the house ofcourse)

* Singleplay fixes:
-- Battle-mode now has correct HP player handling.
-- Using quick-slot items reduces the spell count on the server-side.

* Inventory fixes:
-- You can equip cloths and quick slot items and they are saved.
-- If a quick slot is used up then it gets removed rigtly.

* Login fixes:
-- Logging out resets your login state correctly.
-- If crashing, the disconnect is handled correctly by the server, that, your login state doesn't need to reseted anymore. (Ofcourse may testes on different User's PC give different result, you can report the case.)

* Restrictions
-- Everything related to rooms, creating them, joining and starting game is not implemented under the main branches anymore and will come in future with proper fixes and partially working matchplay.

### 2020-01-29

* Game server structure fixed.
* Minor fixed for room list.

### 2020-01-20

* Added time limited item from inventory.
* Fixed home place item.

### 2020-01-10

* Updated `hibernate-validator` dependency from `6.0.13` to `6.1.0`.
* Updated for match play mechanics.

### 2020-01-07

* Special characters are now allowed inside the chat room.

### 2019-12-16

* Fixed disconnect bug in sessions.
* Fixed lobby user list.
* Fixed login bugs when having multiple characters.
* Added deuce/advantage gameplay.
* Added quick equip item mechanics.

### 2019-12-12

* Added gold boost for new characters.
* Fixed bug battle mode and item reward.

### 2019-12-11

* Fixed some health bar bug.

### 2019-12-09

* Initial version base on [AnCoFT](https://github.com/AnCoFT/AnCoFT).

