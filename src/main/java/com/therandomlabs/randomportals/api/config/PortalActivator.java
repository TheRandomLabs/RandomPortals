package com.therandomlabs.randomportals.api.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

public final class PortalActivator {
	private static final IForgeRegistry<Item> ITEM_REGISTRY = GameRegistry.findRegistry(Item.class);

	public String registryName;
	public int meta = OreDictionary.WILDCARD_VALUE;

	private transient boolean itemRetrieved;
	private transient Item item;
	private transient PortalActivator[] items;

	public PortalActivator() {}

	public PortalActivator(Item item, int meta) {
		registryName = item.getRegistryName().toString();
		this.meta = meta;

		itemRetrieved = true;
		this.item = item;
		items = new PortalActivator[] {
				this
		};
	}

	@Override
	public String toString() {
		return "PortalActivator[registryName=" + registryName + ",meta=" + meta + "]";
	}

	public Item getItem() {
		if(!itemRetrieved && !registryName.startsWith("ore:")) {
			item = ITEM_REGISTRY.getValue(new ResourceLocation(registryName));

			if(item != null) {
				registryName = item.getRegistryName().toString();
			}

			itemRetrieved = true;
		}

		return item;
	}

	public PortalActivator getActualItem() {
		return getItems().length == 0 ? null : items[0];
	}

	public boolean isValid() {
		return getItems().length != 0;
	}

	public boolean test(ItemStack stack) {
		final Item item = stack.getItem();
		final int meta = stack.getMetadata();

		for(PortalActivator activator : getItems()) {
			if(activator.getItem() == item &&
					(activator.meta == OreDictionary.WILDCARD_VALUE || activator.meta == meta)) {
				return true;
			}
		}

		return false;
	}

	private PortalActivator[] getItems() {
		if(items != null) {
			return items;
		}

		if(!registryName.startsWith("ore:")) {
			if(getItem() == null) {
				items = new PortalActivator[0];
				return items;
			}

			items = new PortalActivator[] {
					this
			};
			return items;
		}

		final List<ItemStack> ores = OreDictionary.getOres(registryName.substring(4));

		if(ores.isEmpty()) {
			items = new PortalActivator[0];
			return items;
		}

		final List<PortalActivator> items = new ArrayList<>(ores.size());

		for(ItemStack ore : ores) {
			items.add(new PortalActivator(ore.getItem(), ore.getMetadata()));
		}

		this.items = items.toArray(new PortalActivator[0]);
		return this.items;
	}
}
