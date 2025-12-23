import pymysql
import json
import logging
import time
from datetime import datetime

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler("migration.log", encoding='utf-8'),
        logging.StreamHandler()
    ]
)

SOURCE_DB = {
    'host': 'localhost',
    'user': 'root',
    'password': '123456',
    'database': 'learning_checkin',
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

TARGET_DB = {
    'host': 'localhost',
    'user': 'root',
    'password': '123456',
    'database': 'learning_checkin_optimized',
    'charset': 'utf8mb4',
    'autocommit': True
}

BATCH_SIZE = 1000

def get_connection(config):
    return pymysql.connect(**config)

def migrate_simple_table(source_conn, target_conn, table_name, target_table_name=None):
    if target_table_name is None:
        target_table_name = table_name
        
    logging.info(f"Starting migration for table: {table_name} -> {target_table_name}")
    
    try:
        with source_conn.cursor() as s_cursor, target_conn.cursor() as t_cursor:
            # Check if source table exists
            try:
                s_cursor.execute(f"SELECT COUNT(*) as count FROM {table_name}")
                total_records = s_cursor.fetchone()['count']
            except pymysql.err.ProgrammingError:
                logging.warning(f"Source table {table_name} does not exist. Skipping.")
                return

            if total_records == 0:
                logging.info(f"Table {table_name} is empty. Skipping.")
                return

            logging.info(f"Total records to migrate: {total_records}")
            
            # Get source columns
            s_cursor.execute(f"SHOW COLUMNS FROM {table_name}")
            source_cols = [row['Field'] for row in s_cursor.fetchall()]
            
            # Get target columns
            t_cursor.execute(f"SHOW COLUMNS FROM {target_table_name}")
            target_cols = [row[0] for row in t_cursor.fetchall()] # target uses default cursor (tuple) or I need to check
            # Wait, I didn't set cursorclass for target, so it returns tuples. index 0 is Field.
            
            # Find common columns
            common_cols = [c for c in source_cols if c in target_cols]
            
            if not common_cols:
                logging.error(f"No common columns found for {table_name}. Skipping.")
                return
                
            col_names = ', '.join([f"`{c}`" for c in common_cols])
            placeholders = ', '.join(['%s'] * len(common_cols))
            
            s_cursor.execute(f"SELECT {col_names} FROM {table_name}")
            
            processed = 0
            while True:
                rows = s_cursor.fetchmany(BATCH_SIZE)
                if not rows:
                    break
                
                values = []
                for row in rows:
                    values.append([row[c] for c in common_cols])
                
                sql = f"INSERT IGNORE INTO {target_table_name} ({col_names}) VALUES ({placeholders})"
                
                t_cursor.executemany(sql, values)
                processed += len(rows)
                logging.info(f"Processed {processed}/{total_records} records for {table_name}")
                
            logging.info(f"Finished migration for {table_name}. Total: {processed}")
            
    except Exception as e:
        logging.error(f"Error migrating {table_name}: {e}")

def migrate_study_plan_tasks(source_conn, target_conn):
    logging.info("Starting migration for sys_study_plan_task")
    try:
        with source_conn.cursor() as s_cursor, target_conn.cursor() as t_cursor:
            s_cursor.execute("SELECT * FROM sys_study_plan_task")
            
            processed = 0
            while True:
                rows = s_cursor.fetchmany(BATCH_SIZE)
                if not rows: break
                
                values = []
                for row in rows:
                    # Map 'content' to 'title', set 'description' to NULL or empty
                    title = row.get('content')
                    # If content is too long for title (varchar 255), truncate it
                    if title and len(title) > 255:
                        title = title[:252] + "..."
                        
                    values.append((
                        row.get('id'),
                        row.get('plan_id'),
                        title,
                        row.get('standard'),
                        row.get('deadline'),
                        row.get('status'),
                        row.get('create_time')
                    ))
                
                # Insert into new table
                # New table structure: id, plan_id, title, description, standard, priority, deadline, status, create_time
                sql = """INSERT IGNORE INTO sys_study_plan_task 
                        (id, plan_id, title, standard, deadline, status, create_time) 
                        VALUES (%s, %s, %s, %s, %s, %s, %s)"""
                        
                t_cursor.executemany(sql, values)
                processed += len(rows)
                logging.info(f"Processed {processed} records for sys_study_plan_task")
                
            logging.info(f"Finished migration for sys_study_plan_task. Total: {processed}")
    except Exception as e:
        logging.error(f"Error migrating sys_study_plan_task: {e}")

def migrate_logs(source_conn, target_conn):
    logging.info("Starting migration for merged logs (sys_log)")
    
    with source_conn.cursor() as s_cursor, target_conn.cursor() as t_cursor:
        # 1. Login Logs
        logging.info("Migrating sys_login_log -> sys_log (LOGIN)")
        try:
            s_cursor.execute("SELECT * FROM sys_login_log")
            while True:
                rows = s_cursor.fetchmany(BATCH_SIZE)
                if not rows: break
                
                values = []
                for row in rows:
                    extra = json.dumps({'username': row.get('username'), 'device': row.get('device')}, ensure_ascii=False)
                    values.append((
                        'LOGIN', row.get('user_id'), 'Auth', 'Login', 
                        row.get('message', 'Login attempt'), row.get('ip'), 
                        row.get('status'), 0, extra, row.get('create_time')
                    ))
                
                sql = """INSERT IGNORE INTO sys_log 
                        (log_type, user_id, module, action, content, ip, status, execution_time, extra_info, create_time) 
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
                t_cursor.executemany(sql, values)
        except Exception as e:
            logging.error(f"Error migrating login logs: {e}")

        # 2. Operation Logs
        logging.info("Migrating sys_operation_log -> sys_log (OPERATION)")
        try:
            s_cursor.execute("SELECT * FROM sys_operation_log")
            while True:
                rows = s_cursor.fetchmany(BATCH_SIZE)
                if not rows: break
                
                values = []
                for row in rows:
                    extra = json.dumps({'method': row.get('method')}, ensure_ascii=False)
                    values.append((
                        'OPERATION', row.get('user_id'), row.get('module'), row.get('action'), 
                        row.get('description'), row.get('ip'), 
                        row.get('status'), row.get('execution_time'), extra, row.get('create_time')
                    ))
                
                sql = """INSERT IGNORE INTO sys_log 
                        (log_type, user_id, module, action, content, ip, status, execution_time, extra_info, create_time) 
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
                t_cursor.executemany(sql, values)
        except Exception as e:
            logging.error(f"Error migrating operation logs: {e}")

        # 3. Sensitive Logs
        logging.info("Migrating sys_sensitive_log -> sys_log (SENSITIVE)")
        try:
            s_cursor.execute("SELECT * FROM sys_sensitive_log")
            while True:
                rows = s_cursor.fetchmany(BATCH_SIZE)
                if not rows: break
                
                values = []
                for row in rows:
                    extra = json.dumps({
                        'detected_words': row.get('detected_words'),
                        'source_type': row.get('source_type'),
                        'source_id': row.get('source_id')
                    }, ensure_ascii=False)
                    values.append((
                        'SENSITIVE', row.get('user_id'), 'ContentAudit', 'SensitiveWordHit', 
                        row.get('content_snippet'), None, 
                        1, 0, extra, row.get('create_time')
                    ))
                
                sql = """INSERT IGNORE INTO sys_log 
                        (log_type, user_id, module, action, content, ip, status, execution_time, extra_info, create_time) 
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
                t_cursor.executemany(sql, values)
        except Exception as e:
            logging.error(f"Error migrating sensitive logs: {e}")

def migrate_notices(source_conn, target_conn):
    logging.info("Starting migration for notices (sys_notice)")
    
    with source_conn.cursor() as s_cursor, target_conn.cursor() as t_cursor:
        # 1. Notifications
        logging.info("Migrating sys_notification -> sys_notice (NOTIFICATION)")
        try:
            s_cursor.execute("SELECT * FROM sys_notification")
            while True:
                rows = s_cursor.fetchmany(BATCH_SIZE)
                if not rows: break
                
                values = []
                for row in rows:
                    values.append((
                        'NOTIFICATION', 0, row.get('user_id'), 
                        row.get('title', 'Notification'), row.get('content'), 
                        row.get('is_read', 0), row.get('create_time')
                    ))
                
                sql = """INSERT IGNORE INTO sys_notice 
                        (type, sender_id, receiver_id, title, content, is_read, create_time) 
                        VALUES (%s, %s, %s, %s, %s, %s, %s)"""
                t_cursor.executemany(sql, values)
        except Exception as e:
            logging.error(f"Error migrating notifications: {e}")

        # 2. Announcements
        logging.info("Migrating sys_announcement -> sys_notice (ANNOUNCEMENT)")
        try:
            s_cursor.execute("SELECT * FROM sys_announcement")
            while True:
                rows = s_cursor.fetchmany(BATCH_SIZE)
                if not rows: break
                
                values = []
                for row in rows:
                    values.append((
                        'ANNOUNCEMENT', 0, None, 
                        row.get('title'), row.get('content'), 
                        0, row.get('create_time')
                    ))
                
                sql = """INSERT IGNORE INTO sys_notice 
                        (type, sender_id, receiver_id, title, content, is_read, create_time) 
                        VALUES (%s, %s, %s, %s, %s, %s, %s)"""
                t_cursor.executemany(sql, values)
        except Exception as e:
            logging.error(f"Error migrating announcements: {e}")

def migrate_configs(source_conn, target_conn):
    logging.info("Migrating sys_points_rule -> sys_config")
    try:
        with source_conn.cursor() as s_cursor, target_conn.cursor() as t_cursor:
            # Clean up potential garbage from previous run
            t_cursor.execute("DELETE FROM sys_config WHERE config_key = 'points_rule_None'")
            
            s_cursor.execute("SELECT * FROM sys_points_rule")
            rows = s_cursor.fetchall()
            
            for row in rows:
                # Source columns: rule_key, rule_value, description
                key = f"points_rule_{row.get('rule_key')}"
                value = str(row.get('rule_value'))
                desc = row.get('description')
                
                # Check if exists
                t_cursor.execute("SELECT id FROM sys_config WHERE config_key = %s", (key,))
                if t_cursor.fetchone():
                    t_cursor.execute("UPDATE sys_config SET config_value=%s, description=%s WHERE config_key=%s", (value, desc, key))
                else:
                    t_cursor.execute("INSERT INTO sys_config (config_key, config_value, description) VALUES (%s, %s, %s)", (key, value, desc))
            
            logging.info(f"Migrated {len(rows)} point rules.")
    except Exception as e:
        logging.error(f"Error migrating point rules: {e}")

def verify_migration(source_conn, target_conn):
    logging.info("Verifying migration...")
    tables = [
        ('sys_user', 'sys_user'),
        ('sys_course', 'sys_course'),
        ('sys_task', 'sys_task'),
        ('sys_checkin', 'sys_checkin'),
        ('sys_points_record', 'sys_points_record'),
        ('sys_product', 'sys_product'),
        ('sys_order', 'sys_order'),
        ('sys_friendship', 'sys_friendship'),
        ('sys_friend_request', 'sys_friend_request'),
        ('sys_course_student', 'sys_course_student'),
        ('sys_study_plan', 'sys_study_plan'),
        ('sys_study_plan_task', 'sys_study_plan_task'),
        ('sys_study_plan_progress_history', 'sys_study_plan_progress_history'),
        ('sys_task_submission', 'sys_task_submission'),
        ('sys_task_checkin', 'sys_task_checkin')
    ]
    
    with source_conn.cursor() as s_cursor, target_conn.cursor() as t_cursor:
        for s_table, t_table in tables:
            try:
                # Check if source table exists first
                try:
                    s_cursor.execute(f"SELECT COUNT(*) as c FROM {s_table}")
                    s_count = s_cursor.fetchone()['c']
                except:
                    logging.warning(f"Source table {s_table} does not exist (in verification).")
                    continue
                    
                t_cursor.execute(f"SELECT COUNT(*) as c FROM {t_table}")
                t_count = t_cursor.fetchone()[0]
                
                status = "MATCH" if s_count <= t_count else "MISMATCH"
                logging.info(f"Table {s_table}: Source={s_count}, Target={t_count} [{status}]")
                if status == "MISMATCH":
                     logging.warning(f"Warning: {s_table} count mismatch! (Diff: {s_count - t_count})")
            except Exception as e:
                logging.warning(f"Could not verify {s_table}: {e}")

def main():
    logging.info("Starting Data Migration Process...")
    start_time = time.time()
    
    s_conn = None
    t_conn = None
    
    try:
        s_conn = get_connection(SOURCE_DB)
        t_conn = get_connection(TARGET_DB)
        logging.info("Connected to Source and Target Databases.")
        
        # 1. Simple Tables (Auto mapping common columns)
        simple_tables = [
            'sys_user', 'sys_course', 'sys_task', 'sys_checkin', 
            'sys_points_record', 'sys_product', 'sys_order', 
            'sys_friendship', 'sys_friend_request', 'sys_course_student',
            'sys_study_plan', 'sys_study_plan_progress_history',
            'sys_task_submission', 'sys_task_checkin'
        ]
        
        for table in simple_tables:
            migrate_simple_table(s_conn, t_conn, table)
            
        # 2. Custom Mapped Tables
        migrate_study_plan_tasks(s_conn, t_conn)
            
        # 3. Merged Tables
        migrate_logs(s_conn, t_conn)
        migrate_notices(s_conn, t_conn)
        
        # 4. Configs
        migrate_configs(s_conn, t_conn)
        
        # 5. Verification
        verify_migration(s_conn, t_conn)
        
    except Exception as e:
        logging.error(f"Critical Error: {e}")
    finally:
        if s_conn and s_conn.open: s_conn.close()
        if t_conn and t_conn.open: t_conn.close()
        
    end_time = time.time()
    logging.info(f"Migration completed in {end_time - start_time:.2f} seconds.")

if __name__ == "__main__":
    main()
