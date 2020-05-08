
package com.damaru.store.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemViewArray {

	public ItemViewArray () {
	}
	public ItemViewArray (ItemView[] itemView) {
		this.itemView = itemView;
	}
	private ItemView[] itemView;

	public ItemView[] getItemView() { return itemView; }
	public void setItemView(ItemView[] itemView) { this.itemView = itemView; }


	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ItemView {

		public ItemView () {
		}
		public ItemView (
			Double price, 
			String description, 
			Integer id, 
			String category) {
			this.price = price;
			this.description = description;
			this.id = id;
			this.category = category;
		}


		private Double price;
		private String description;
		private Integer id;
		private String category;

		public Double getPrice() {
			return price;
		}

		public ItemView setPrice(Double price) {
			this.price = price;
			return this;
		}


		public String getDescription() {
			return description;
		}

		public ItemView setDescription(String description) {
			this.description = description;
			return this;
		}


		public Integer getId() {
			return id;
		}

		public ItemView setId(Integer id) {
			this.id = id;
			return this;
		}


		public String getCategory() {
			return category;
		}

		public ItemView setCategory(String category) {
			this.category = category;
			return this;
		}


		public String toString() {
			return "ItemView ["
			+ " price: " + price
			+ " description: " + description
			+ " id: " + id
			+ " category: " + category
			+ " ]";
		}
	}



	public String toString() {
		return "ItemViewArray ["
		+ " ]";
	}
}

