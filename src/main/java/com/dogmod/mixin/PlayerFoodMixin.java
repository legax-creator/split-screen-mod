private static ActionResult feed(ServerPlayerEntity owner, ServerPlayerEntity dog,
        DogData data, ItemStack meat) {
    // Canı değil açlığı doldur
    net.minecraft.entity.player.HungerManager hunger = dog.getHungerManager();
    
    // Et başına +4 açlık puanı (vanilla köpek gibi)
    int newFood = Math.min(20, hunger.getFoodLevel() + 4);
    hunger.setFoodLevel(newFood);
    // Doyma (saturation) da ekle
    hunger.setSaturationLevel(Math.min(hunger.getSaturationLevel() + 2.0f, newFood));

    if (!owner.isCreative()) meat.decrement(1);

    playSound(dog, SoundEvents.ENTITY_WOLF_AMBIENT, 1.2f);
    dog.sendMessage(Text.literal("§c🍖 Sahibin seni besledi!"), true);
    owner.sendMessage(Text.literal("§a" + dog.getName().getString() + " beslendi!"), false);
    return ActionResult.SUCCESS;
}
