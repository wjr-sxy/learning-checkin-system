import mysql.connector
import sys

try:
    conn = mysql.connector.connect(
        host="localhost",
        user="root",
        password="123456",
        database="learning_checkin"
    )
    cursor = conn.cursor()
    cursor.execute("SELECT id, username, password, role FROM sys_user")
    rows = cursor.fetchall()
    print(f"Total users: {len(rows)}")
    for row in rows:
        print(row)
    
    conn.close()
except Exception as e:
    print(f"Error: {e}")
