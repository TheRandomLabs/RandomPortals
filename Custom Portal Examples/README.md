# Custom Portal Examples

Each portal type group is defined in its own directory under `config/randomportals/portal_types`.
Each portal type group directory needs a `group_data.json` and at least one portal type JSON.
Portal type JSONs are named `<Activation dimension ID>.json`.

`reference_portal` should be used as a reference.

`vanilla_nether_portal` is the default vanilla Nether portal.

`one_way_portal` defines a standard obsidian portal for the overworld, but when entered, the
generated portal cannot be entered to return to the overworld. Instead, a separate sponge
portal must be activated, and when entered, the generated sponge portal in the overworld
cannot be entered to return to the Nether. You can see this portal type group in action
[here](https://gfycat.com/FittingSecondaryHoiho).

Note that `vanilla_nether_portal` only defines a portal type for the dimension with ID `0`.
If a player goes through a vanilla Nether portal from the Nether, even though the destination
dimension ID is set to `-1`, RandomPortals recognizes that the player is already in the Nether
and uses the default dimension ID defined in `group_data.json`.
