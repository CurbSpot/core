package com.foodtruckopt.dto;

public class ItemDetails {
	String ecvs;
	String product;
	String item;
	Long ecvsId = null;
	Long productId= null;
	Long itemId= null;

	public String getEcvs() {
		return ecvs;
	}

	public String getProduct() {
		return product;
	}

	public Long getItemId() {
		return itemId;
	}

	public Long getEcvsId() {
		return ecvsId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public void setEcvsId(Long ecvsId) {
		this.ecvsId = ecvsId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getItem() {
		return item;
	}

	public ItemDetails(String evcs, String product, String item) {
		this.ecvs = evcs;
		this.product = product;
		this.item = item;
	}

	public String toString() {
		return nvl(ecvs) + "," + nvl(product) + "," + nvl(item) + "," + nvl(ecvsId) + "," + nvl(productId) + "," + nvl(itemId) + ","
				+ nvl(ecvsExists) + "," + nvl(productExists) + "," + nvl(itemExists);
	}

	String nvl(Object obj) {
		return obj == null ? "" : obj.toString();
	}

	boolean ecvsExists;

	public boolean isEcvsExists() {
		return ecvsExists;
	}

	boolean productExists;

	public boolean isProductExists() {
		return productExists;
	}

	boolean itemExists;

	public boolean isItemExists() {
		return itemExists;
	}

	public void setEcvsExists(boolean ecvsExists) {
		this.ecvsExists = ecvsExists;
	}

	public void setProductExists(boolean productExists) {
		this.productExists = productExists;
	}

	public void setItemExists(boolean itemExists) {
		this.itemExists = itemExists;
	}
}
