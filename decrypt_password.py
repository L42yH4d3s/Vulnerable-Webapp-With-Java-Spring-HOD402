import bcrypt
import re

def parse_users_data(file_path):
    
    users = []
    current_user = {}
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            for line in lines:
                line = line.strip()
                if not line or line.startswith("=") or line.startswith("-"):
                    continue  # Bỏ qua dòng trống hoặc phân tách
                parts = line.split(": ", 1) if ": " in line else [line, ""]
                if len(parts) < 2:
                    print(f"[-] Dòng không hợp lệ, bỏ qua: {line}")
                    continue
                key, value = parts
                if key == "User ID":
                    if current_user:  # Lưu user trước đó nếu có
                        users.append(current_user)
                    current_user = {"id": value}
                elif key == "Username":
                    current_user["username"] = value
                elif key == "Password":
                    current_user["password"] = value
            if current_user:  # Lưu user cuối cùng
                users.append(current_user)
    except FileNotFoundError:
        print(f"[-] File {file_path} không tồn tại.")
        return []
    except Exception as e:
        print(f"[-] Lỗi khi đọc file {file_path}: {e}")
        return []
    return users

def load_password_list(wordlist_path):
    """Đọc danh sách mật khẩu từ file wordlist."""
    try:
        with open(wordlist_path, 'r', encoding='utf-8', errors='ignore') as f:
            return [line.strip() for line in f if line.strip()]
    except FileNotFoundError:
        print(f"[-] File {wordlist_path} không tồn tại, sử dụng danh sách mặc định.")
        return [
            "password123", "admin", "test123", "secret", "user3",
            "admin123", "admin2025", "seller1", "seller1123", "seller12025",
            "seller2", "seller2123", "seller22025", "seller3", "seller3123",
            "seller32025", "user", "user123", "user2025", "user2", "user2123",
            "user22025", "password", "123456", "qwerty", "letmein", "welcome"
        ]

def check_password(raw_password, hashed_password):
    """Kiểm tra mật khẩu với chuỗi băm."""
    try:
        # Ensure inputs are strings, encode to bytes only here
        if isinstance(raw_password, bytes):
            raw_password = raw_password.decode('utf-8')
        if isinstance(hashed_password, bytes):
            hashed_password = hashed_password.decode('utf-8')
        return bcrypt.checkpw(raw_password.encode('utf-8'), hashed_password.encode('utf-8'))
    except Exception as e:
        print(f"[-] Lỗi khi kiểm tra mật khẩu '{raw_password}': {e}")
        return False

def brute_force_password(hashed_password, password_list, username=None):
    """Thử tất cả mật khẩu trong danh sách để tìm mật khẩu gốc."""
    context_passwords = []
    if username and username.strip():
        context_passwords = [
            username, username + "123", username + "2025",
            username.lower(), username.lower() + "123"
        ]
    all_passwords = context_passwords + password_list
    for raw_password in all_passwords:
        if check_password(raw_password, hashed_password):
            return raw_password
    return None

def save_results(results, output_file="decrypted_results.txt"):
    """Lưu kết quả vào file."""
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("KẾT QUẢ GIẢI MÃ MẬT KHẨU\n")
        f.write("=" * 50 + "\n")
        for user in results:
            f.write(f"User ID: {user['id']}\n")
            f.write(f"Username: {user.get('username', '')}\n")
            f.write(f"Hashed Password: {user.get('password', '')}\n")
            f.write(f"Decrypted Password: {user.get('decrypted_password', 'Not found')}\n")
            f.write("-" * 50 + "\n")
    print(f"[+] Kết quả đã được lưu vào {output_file}")

def main():
    # Đường dẫn file
    users_data_file = "users_data.txt"
    wordlist_file = "wordlist.txt"
    
    # Đọc dữ liệu user từ file
    print("[+] Đọc dữ liệu từ file users_data.txt...")
    users = parse_users_data(users_data_file)
    if not users:
        print("[-] Không tìm thấy user nào trong file.")
        return
    
    # Đọc danh sách mật khẩu
    print("[+] Đọc danh sách mật khẩu từ wordlist...")
    password_list = load_password_list(wordlist_file)
    
    # Kiểm tra mật khẩu cho từng user
    results = []
    for user in users:
        print(f"\n[+] Kiểm tra mật khẩu cho User ID: {user['id']} (Username: {user.get('username', '')})")
        hashed_password = user.get('password', '')
        
        if not hashed_password or not hashed_password.startswith('$2a$'):
            print(f"[-] Chuỗi băm không hợp lệ hoặc rỗng: {hashed_password}")
            user['decrypted_password'] = "Invalid or empty hash"
            results.append(user)
            continue
        
        # Thử brute-force mật khẩu, pass hashed_password as string
        decrypted_password = brute_force_password(
            hashed_password,  # Pass as string, not bytes
            password_list,
            user.get('username')
        )
        
        if decrypted_password:
            print(f"[+] Mật khẩu tìm thấy: {decrypted_password}")
            user['decrypted_password'] = decrypted_password
        else:
            print("[-] Không tìm thấy mật khẩu.")
            user['decrypted_password'] = "Not found"
        
        results.append(user)
    
    # Lưu kết quả
    save_results(results)

if __name__ == "__main__":
    main()