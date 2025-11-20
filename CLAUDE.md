# Tinkers' Old Guns - Development Context

## Project Overview
A Tinkers' Construct 1.20.1 addon that integrates Old Guns mod weapons as craftable TC tools.

## Main Goals
- Add 3 gun-type tools: Pistol, Carbine, Musket
- Use Old Guns ammo system (Small/Medium/Large)
- Integrate Old Guns reload mechanics
- Support TC modifiers + custom gun modifiers

## Tool Specifications

### Pistol
- **Parts**: Bow Limb (barrel), Handle (grip), Flintlock Mechanism
- **Ammo**: Small
- **Behavior**: Fast reload, lower damage

### Carbine
- **Parts**: Bow Limb (barrel), Tough Handle, Plate, Flintlock Mechanism
- **Ammo**: Medium
- **Behavior**: Balanced stats

### Musket
- **Parts**: Bow Limb (barrel), Tough Handle, Plate, Flintlock Mechanism
- **Ammo**: Large
- **Behavior**: Slow reload, high damage

## Custom Modifiers to Implement
1. **Extra Barrel** - Adds extra ammo slots
2. **Scattershot** - Fires spread of weaker projectiles (Blunderbuss-style)
3. **Subaquatic** - Allows underwater firing
4. **Simultaneous** - Fires all loaded shots at once (Nock Gun-style)

## Progress Tracking
- [x] Project setup and cleanup
- [x] Source code analysis (Old Guns & Tinkers' Construct)
- [x] Architecture design
- [x] Core implementation (Phase 1-3 complete)
- [ ] Testing and polish
- [ ] Balance adjustments

## Implementation Status

### ✅ Completed Components

#### Core Systems
- ✅ `AmmoSize` enum - Defines Small/Medium/Large ammo categories
- ✅ `GunAmmoHelper` - Bridges Old Guns ammo system with TC tools
  - Ammo validation and loading
  - Ammo capacity calculation (base + Extra Barrel modifier)
  - Projectile firing with TC stat application
  - Tooltip generation for ammo info

#### Gun Tool Items
- ✅ `ModifiableGunItem` - Base class for all gun tools
  - Extends `ModifiableLauncherItem`
  - Integrates Old Guns ammo system
  - Custom tooltips showing ammo count and type
  - Reload cooldown based on DRAW_SPEED stat
- ✅ `ModifiablePistolItem` - Small ammo, fast reload (0.8x)
- ✅ `ModifiableCarbineItem` - Medium ammo, normal reload (1.0x)
- ✅ `ModifiableMusketItem` - Large ammo, slow reload (1.3x)

#### Tool Parts
- ✅ `FlintlockMechanismItem` - Custom gun part using HandleMaterialStats

#### Custom Modifiers
- ✅ `ExtraBarrelModifier` - Adds +1 ammo capacity per level (max 3)
- ✅ `ScattershotModifier` - Fires 3 projectiles in spread (-40% damage each)
- ✅ `SubaquaticModifier` - Reduces water penalty (50% WATER_INERTIA)
- ✅ `SimultaneousModifier` - Fires all loaded shots at once

#### Registries
- ✅ `TinkersGunTools` - Tool registration
- ✅ `TinkersGunParts` - Part registration
- ✅ `TinkersGunModifiers` - Modifier registration
- ✅ `TinkersGunDefinitions` - Tool definition constants

#### Data Files
- ✅ Tool definitions (pistol.json, carbine.json, musket.json)
- ✅ Modifier recipes (all 4 custom modifiers)
- ✅ Part recipes (flintlock_mechanism)
- ✅ Item tags (guns, parts)
- ✅ Language file (en_us.json)

#### Creative Tabs
- ✅ `TinkersGunTabs` - Creative tab registration
- ✅ **Tinker's Old Guns** tab - Shows all gun tools with material variants
- ✅ **Tinker's Old Gun Parts** tab - Shows flintlock mechanism with all material variants

### 🎯 Key Features Implemented

#### Tooltip System Integration
The tooltip system combines **both TC and Old Guns information**:
- TC base tooltips (durability, stats, modifiers)
- Ammo count display: "Ammo: X / Y"
- Loaded ammo type: "Loaded: [Ammo Name]"
- Ammo size requirement: "Uses Small/Medium/Large Ammo"
- Modifier descriptions with custom tooltips

#### Ammo Integration
- Uses Old Guns' NBT list system for ammo storage
- Validates ammo type based on gun size
- Supports all Old Guns ammo variants (stone/iron/lead, musket ball/birdshot/buckshot)
- Ammo capacity affected by Extra Barrel modifier

#### Stat Application
TC material stats modify Old Guns projectiles:
- **VELOCITY** → Projectile speed multiplier
- **PROJECTILE_DAMAGE** → Damage multiplier
- **DRAW_SPEED** → Reload cooldown
- **ACCURACY** → Deviation (lower = better)
- **DURABILITY** → Uses consumed per shot

### 📋 Remaining Tasks

#### Testing
- [ ] Test gun crafting at Tinker's Anvil
- [ ] Test ammo loading mechanics
- [ ] Test projectile firing and damage
- [ ] Test all 4 custom modifiers
- [ ] Test TC material stat effects on projectiles
- [ ] Test compatibility with TC trait system

#### Balance & Polish
- [ ] Fine-tune damage values
- [ ] Adjust reload speeds
- [ ] Balance modifier costs
- [ ] Add custom sounds for firing/reloading
- [ ] Create textures and models
- [ ] Add tool station integration for repair

#### Reload System
✅ **IMPLEMENTED via Mixin**
- Created `ShapelessVanillaMuzzleloaderReloadRecipeMixin` to hook into Old Guns reload recipes
- Tinkers guns can now be reloaded using the same crafting system as Old Guns
- Simply craft: Gun + Ammo → Loaded Gun
- Works with all Old Guns ammo types (Small/Medium/Large, Stone/Iron/Lead, Musket Ball/Birdshot/Buckshot)

#### Known Limitations
- No custom animations yet (uses bow animation)
- Flintlock mechanism uses HandleMaterialStats (could create custom stat type)
- Scattershot creates extra projectiles on launch (works but not optimized)

## Source Code Analysis Summary

### Old Guns Mod Structure

#### Ammo System
- **Key Files**:
  - Base: `oldguns-1.20.1/src/main/java/com/zach2039/oldguns/api/ammo/Ammo.java`
  - Firearm Ammo: `oldguns-1.20.1/src/main/java/com/zach2039/oldguns/api/ammo/FirearmAmmo.java`
  - Ammo Types: `oldguns-1.20.1/src/main/java/com/zach2039/oldguns/api/ammo/AmmoTypes.java`
  - Implementation: `oldguns-1.20.1/src/main/java/com/zach2039/oldguns/world/item/ammo/firearm/FirearmAmmoItem.java`

- **Ammo Categories**: Small, Medium, Large
- **Ammo Properties**:
  - ProjectileType (MUSKET_BALL, BIRDSHOT, BUCKSHOT)
  - ProjectileSize, ProjectileCount, ProjectileDamage
  - ProjectileDeviationModifier (accuracy)
  - ProjectileEffectiveRange
  - ProjectileArmorBypassPercentage

#### Reload System
- **Reload Types**: MUZZLELOADER (front-loading), BREECHLOADER (rear-loading)
- **NBT Storage**: `FirearmNBTHelper` manages ammo as NBT list on ItemStack
  - `pushNBTTagAmmo()`, `popNBTTagAmmo()`, `peekNBTTagAmmo()`
  - Stack-based ammo management
- **Reload Recipes**: Uses crafting recipes to combine firearm + ammo

#### Gun Items
- **Firearm Sizes**: SMALL (Pistol), MEDIUM (Carbine), LARGE (Musket), HUGE (Nock Gun)
- **Mechanism Types**: MATCHLOCK, WHEELLOCK, FLINTLOCK, CAPLOCK
- **Key Interface**: `Firearm` interface
- **Base Class**: `FirearmItem`
- **Examples**:
  - Matchlock Pistol: 1 shot, small size
  - Flintlock Musket: 1 shot, large size
  - Flintlock Pepperbox: 4 shots
  - Flintlock Nock Gun: 5 shots, fires simultaneously

#### Projectiles
- **Entity**: `BulletProjectile` extends Minecraft's `Projectile`
- **Properties**: Size, damage, effective range, armor bypass percentage
- **Creation**: `FirearmAmmoItem.createProjectiles()` → `FirearmItem.fireProjectiles()`
- **Enchantments**: Supports Power, Punch, Flame

### Tinkers' Construct Tool System

#### Tool Registration
- **Location**: `TinkersConstruct-1.20.1/src/main/java/slimeknights/tconstruct/tools/TinkerTools.java`
- **Pattern**: Register as `ItemObject<T>` where T extends `ModifiableItem`
- **Ranged Examples**: 
  - Crossbow: `ModifiableCrossbowItem`
  - Longbow: `ModifiableBowItem`

#### Tool Definitions
- **Location**: `TinkersConstruct-1.20.1/src/main/java/slimeknights/tconstruct/tools/ToolDefinitions.java`
- **Data Provider**: `TinkersConstruct-1.20.1/src/main/java/slimeknights/tconstruct/tools/data/ToolDefinitionDataProvider.java`
- **Modules**:
  - `PartStatsModule`: Define which parts are used
  - `SetStatsModule`: Set base stats
  - `MultiplyStatsModule`: Apply multipliers

#### Ranged Tool Base Classes
- **ModifiableLauncherItem**: Base for all projectile weapons
  - Abstract methods: `getAllSupportedProjectiles()`, `getUseAnimation()`
- **ModifiableBowItem**: Charge-and-release mechanics
  - Key method: `releaseUsing()`
- **ModifiableCrossbowItem**: Load-then-fire mechanics
  - Key method: `fireCrossbow()`
  - Stores ammo in NBT between loading/firing

#### Tool Parts
- **Location**: `TinkersConstruct-1.20.1/src/main/java/slimeknights/tconstruct/tools/TinkerToolParts.java`
- **Ranged Parts**:
  - `bowLimb`: Uses `LimbMaterialStats.ID` (velocity, draw speed)
  - `bowGrip`: Uses `GripMaterialStats.ID` (accuracy, attack speed)
  - `bowstring`: Uses `StatlessMaterialStats.BOWSTRING` (traits only)

#### Tool Stats
- **VELOCITY**: Projectile speed
- **DRAW_SPEED**: Charge/reload speed
- **PROJECTILE_DAMAGE**: Base projectile damage
- **ACCURACY**: Projectile accuracy (lower = better)
- **DURABILITY**: Tool durability
- **WATER_INERTIA**: Water resistance

#### Modifier System
- **Location**: `TinkersConstruct-1.20.1/src/main/java/slimeknights/tconstruct/tools/TinkerModifiers.java`
- **Base Class**: `Modifier`
- **Hooks**:
  - `ProjectileLaunchModifierHook`: Modify projectiles on launch
  - `BowAmmoModifierHook`: Custom ammo handling
  - `ConditionalStatModifierHook`: Conditional stat modifications
- **Ranged Examples**:
  - Crystalshot: Fires special projectiles
  - Freezing: Adds freeze effect
  - Scope: Improves accuracy

#### Projectiles
- **Entity**: `ModifiableArrow` 
- **Capability**: `EntityModifierCapability` stores modifiers on entity
- **Application**: Modifiers apply effects through hooks

## Integration Strategy

### Approach
1. **Use Old Guns Ammo System**: Keep existing ammo items (Small/Medium/Large)
2. **Create TC Gun Tools**: New tool types that accept TC parts + Flintlock Mechanism
3. **Bridge Projectile Systems**: TC guns fire Old Guns projectiles
4. **Adapt Reload Mechanics**: Integrate Old Guns reload with TC durability/stats
5. **Modifier Integration**: TC modifiers affect Old Guns projectile properties

### Key Challenges
- Old Guns uses NBT lists for ammo storage vs TC's stat system
- Old Guns has complex reload recipes vs TC's instant-use tools
- Projectile creation differs between systems
- Need to preserve Old Guns ammo properties while using TC materials

## Addon Architecture Design

### Package Structure
```
com.leclowndu93150.tinkers_old_guns/
├── common/
│   ├── item/
│   │   ├── gun/               # Gun tool items
│   │   │   ├── ModifiableGunItem.java (base class)
│   │   │   ├── ModifiablePistolItem.java
│   │   │   ├── ModifiableCarbineItem.java
│   │   │   └── ModifiableMusketItem.java
│   │   └── part/              # Gun-specific parts
│   │       └── FlintlockMechanismItem.java
│   ├── modifier/              # Custom gun modifiers
│   │   ├── ExtraBarrelModifier.java
│   │   ├── ScattershotModifier.java
│   │   ├── SubaquaticModifier.java
│   │   └── SimultaneousModifier.java
│   ├── stats/                 # Gun-specific stats
│   │   ├── GunBarrelMaterialStats.java
│   │   └── GunMechanismStats.java
│   └── util/
│       └── GunAmmoHelper.java  # Bridge Old Guns ammo to TC
├── data/
│   ├── ToolDefinitionDataProvider.java
│   └── ModifierRecipeProvider.java
└── registry/
    ├── TinkersGunTools.java
    ├── TinkersGunParts.java
    └── TinkersGunModifiers.java
```

### Tool Design

#### Pistol (Small Gun)
- **Parts**:
  1. Bow Limb (barrel) - Uses `LimbMaterialStats` (velocity, draw speed)
  2. Handle (grip) - Uses `HandleMaterialStats` (durability, accuracy)
  3. Flintlock Mechanism - Custom part (special stats)
- **Stats**:
  - High DRAW_SPEED (fast reload)
  - Low PROJECTILE_DAMAGE
  - Medium VELOCITY
  - Ammo Capacity: 1 (base)
- **Ammo**: Old Guns Small ammo types

#### Carbine (Medium Gun)
- **Parts**:
  1. Bow Limb (barrel)
  2. Tough Handle (stock)
  3. Plate (reinforcement)
  4. Flintlock Mechanism
- **Stats**:
  - Medium DRAW_SPEED
  - Medium PROJECTILE_DAMAGE
  - High VELOCITY
  - Ammo Capacity: 1 (base)
- **Ammo**: Old Guns Medium ammo types

#### Musket (Large Gun)
- **Parts**:
  1. Bow Limb (barrel)
  2. Tough Handle (stock)
  3. Plate (reinforcement)
  4. Flintlock Mechanism
- **Stats**:
  - Low DRAW_SPEED (slow reload)
  - High PROJECTILE_DAMAGE
  - Very High VELOCITY
  - Ammo Capacity: 1 (base)
- **Ammo**: Old Guns Large ammo types

### Ammo Integration System

#### Approach: Hybrid NBT + TC Stats
1. **Ammo Storage**: Use Old Guns' NBT list system (`FirearmNBTHelper`)
2. **Ammo Capacity**: Controlled by TC modifiers (Extra Barrel)
3. **Reload Mechanics**: Simplified crafting recipe or special reload modifier
4. **Projectile Creation**: Use Old Guns' `FirearmAmmoItem.createProjectiles()`
5. **TC Stats Influence**: Modify Old Guns projectile properties based on TC materials

#### Gun Ammo Helper
```java
public class GunAmmoHelper {
    // Check if ammo is compatible with gun
    public static boolean isValidAmmo(ItemStack gun, ItemStack ammo);
    
    // Get ammo capacity based on tool stats + modifiers
    public static int getAmmoCapacity(ToolStack tool);
    
    // Load ammo into gun (uses Old Guns NBT system)
    public static boolean loadAmmo(ToolStack tool, ItemStack ammo);
    
    // Fire projectiles (bridge to Old Guns system)
    public static void fireProjectiles(ToolStack tool, Level level, Player player);
    
    // Apply TC material stats to Old Guns projectiles
    public static void applyToolStatsToProjectile(ToolStack tool, BulletProjectile projectile);
}
```

### Modifier Implementation

#### 1. Extra Barrel Modifier
- **Type**: Upgrade
- **Slots**: 1
- **Effect**: +1 ammo capacity per level (max 3 levels)
- **Hook**: Modifies ammo capacity in `GunAmmoHelper.getAmmoCapacity()`

#### 2. Scattershot Modifier
- **Type**: Ability
- **Slots**: 1
- **Effect**: Fires 3-5 weaker projectiles in a spread pattern
- **Hook**: `ProjectileLaunchModifierHook` - modifies projectile count and damage
- **Trade-off**: -40% damage per projectile, +200° deviation

#### 3. Subaquatic Modifier
- **Type**: Upgrade
- **Slots**: 1
- **Effect**: Allows firing underwater, reduces water slowdown
- **Hook**: Override water checks, modify WATER_INERTIA stat

#### 4. Simultaneous Modifier
- **Type**: Ability
- **Slots**: 2
- **Effect**: If multiple shots loaded, fires all at once
- **Hook**: `ProjectileLaunchModifierHook` - fires all loaded ammo
- **Requirement**: Requires Extra Barrel modifier

### Gun Item Implementation

#### Base Class: ModifiableGunItem
```java
public abstract class ModifiableGunItem extends ModifiableLauncherItem {
    protected final AmmoSize ammoSize; // Small, Medium, Large
    
    public ModifiableGunItem(Properties props, ToolDefinition def, AmmoSize size) {
        super(props, def);
        this.ammoSize = size;
    }
    
    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        // Return predicate for Old Guns ammo matching our size
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        // Fire loaded ammo using Old Guns projectile system
        // Apply TC stats to projectile properties
        // Consume durability
    }
    
    public void reload(ToolStack tool, ItemStack ammo, Player player) {
        // Use GunAmmoHelper to load ammo
        // Play reload animation/sound
    }
    
    protected abstract int getBaseAmmoCapacity();
    protected abstract float getReloadSpeedMultiplier();
}
```

### Flintlock Mechanism Part

#### Special Part System
- **Type**: Custom tool part (like Bowstring)
- **Material Stats**: Custom `MechanismMaterialStats`
  - Durability multiplier
  - Reliability (misfire chance reduction)
  - Water resistance
- **Materials**: Limited to special materials
  - Flintlock (default): Balanced stats
  - Wheellock: Better reliability, worse water resistance
  - Caplock: Best water resistance

### Data Generation

#### Tool Definitions
```java
// Pistol
define(TinkersGunDefinitions.PISTOL)
  .module(PartStatsModule.parts()
    .part(TinkerToolParts.bowLimb)      // Barrel
    .part(TinkerToolParts.handle)       // Grip
    .part(TinkersGunParts.mechanism)    // Flintlock
    .build())
  .module(new SetStatsModule(StatsNBT.builder()
    .set(ToolStats.PROJECTILE_DAMAGE, 5f)
    .set(ToolStats.VELOCITY, 3f)
    .set(ToolStats.DRAW_SPEED, 1.5f)
    .set(ToolStats.ACCURACY, 0.9f)
    .build()))
  .module(new MultiplyStatsModule(MultiplierNBT.builder()
    .set(ToolStats.DURABILITY, 1.5f)
    .build()))
  .smallToolStartingSlots();

// Carbine & Musket similar with 4 parts (+ tough handle, plate)
```

### Integration Points

#### 1. Projectile Creation
- Use Old Guns' `BulletProjectile` entity
- Apply TC material stats as modifiers to projectile properties
- Hook TC modifier system into projectile launch

#### 2. Reload System
- Option A: Crafting recipe (gun + ammo → loaded gun)
- Option B: Special "Reload" modifier that allows in-inventory reload
- Option C: Custom keybind for reload action (more complex)

#### 3. Durability
- Each shot consumes durability (like TC bow)
- Durability based on material stats
- Can be repaired at TC tool station

#### 4. Compatibility
- All normal TC modifiers work (Haste, Reinforced, etc.)
- TC material traits apply (Magnetic, Momentum, etc.)
- Enchantments handled through TC modifier system

### Development Phases

#### Phase 1: Core System
1. Create `ModifiableGunItem` base class
2. Implement `GunAmmoHelper` for Old Guns integration
3. Register Flintlock Mechanism part
4. Register Pistol tool with basic functionality

#### Phase 2: Tool Variants
1. Implement Carbine and Musket
2. Create tool definitions with proper stats
3. Test ammo loading and firing

#### Phase 3: Custom Modifiers
1. Implement Extra Barrel
2. Implement Scattershot
3. Implement Subaquatic
4. Implement Simultaneous

#### Phase 4: Polish & Balance
1. Adjust damage/velocity/accuracy values
2. Create recipes and crafting integration
3. Add textures and models
4. Test compatibility with TC/Old Guns features

## Notes
- Old Guns source available at: `oldguns-1.20.1/`
- Tinkers' Construct source at: `TinkersConstruct-1.20.1/`
- Minecraft source at: `Minecraft-Source-1.20.1/`
