/* Regular tables */

CREATE TABLE publisher (
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    phone TEXT NOT NULL,

    PRIMARY KEY (name)
);

CREATE TABLE country (
    name TEXT NOT NULL,

    PRIMARY KEY (name)
);

CREATE TABLE author (
    name TEXT NOT NULL,
    country_name TEXT NOT NULL,
    birthday DATE NOT NULL,

    PRIMARY KEY (name),
    FOREIGN KEY (country_name) REFERENCES country (name)
);

CREATE TABLE customer (
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    address TEXT NOT NULL,
    phone TEXT NOT NULL,
    basket_id INTEGER NOT NULL,

    PRIMARY KEY (name),
    UNIQUE (basket_id)
);

CREATE TABLE warehouse (
    id INTEGER NOT NULL,
    address TEXT NOT NULL,
    phone TEXT NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE book (
    isbn TEXT NOT NULL,
    title TEXT NOT NULL,
    year INTEGER NOT NULL,
    publisher_name TEXT NOT NULL,

    PRIMARY KEY (isbn),
    FOREIGN KEY (publisher_name) REFERENCES publisher (name)
);

/* Relational tables */

CREATE TABLE written_rel (
    author_name TEXT NOT NULL,
    book_isbn TEXT NOT NULL,

    FOREIGN KEY (author_name) REFERENCES author (name),
    FOREIGN KEY (book_isbn) REFERENCES book (isbn)
);

CREATE TABLE located_rel (
    warehouse_id INTEGER NOT NULL,
    book_isbn TEXT NOT NULL,

    FOREIGN KEY (warehouse_id) REFERENCES warehouse (id),
    FOREIGN KEY (book_isbn) REFERENCES book (isbn)
);

CREATE TABLE buy_rel (
    customer_basket_id INTEGER NOT NULL,
    book_isbn TEXT NOT NULL,
    price INTEGER NOT NULL,
    date DATE NOT NULL,

    FOREIGN KEY (customer_basket_id) REFERENCES customer (basket_id),
    FOREIGN KEY (book_isbn) REFERENCES book (isbn)
);