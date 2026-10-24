package com.example;

class InMemoryUserStoreTest extends UserStoreContract {

  @Override
  protected UserStore createStore() {
    return new InMemoryUserStore();
  }
}
