# Custom Portal Examples

These are some custom portal type examples.

`vanilla_nether_portal` is the default vanilla Nether portal.

`reference_portal` should be used as a reference.

Note that `vanilla_nether_portal` only defines a portal type for the dimension with ID `0`.
If a player goes through a vanilla Nether portal from the Nether, even though the destination
dimension ID is set to `-1`, RandomPortals recognizes that the player is already in the Nether
and uses the default dimension ID defined in `group_data.json`.
