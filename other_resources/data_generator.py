import sqlite3
import random

uuids = []
with open("uuids.txt", "r") as uuids_file:
    for line in uuids_file.readlines():
        uuids.append(line.rstrip())

database_path = input("Sqlite Database Path >")
amount = min(int(input("Amount of users to insert >")), len(uuids))

connection = sqlite3.connect(database_path)
cursor = connection.cursor()

for uuid in random.sample(uuids, amount):
    cursor.execute("INSERT INTO highscores VALUES (?, ?)", (uuid, random.randint(1,100)))

connection.commit()

