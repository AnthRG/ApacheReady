import requests

BASE_URL = "http://localhost:7070"

def login():
    url = f"{BASE_URL}/api/login"
    username = input("Usuario: ")
    password = input("Contraseña: ")
    data = {"username": username, "password": password}

    try:
        response = requests.post(url, data=data)
        if response.status_code == 200:
            token = response.json().get("token")
            print("✅ Login exitoso.")
            return token
        else:
            print("❌ Error en login:", response.text)
    except Exception as e:
        print("🚫 Excepción durante login:", e)
    return None

def list_urls(token):
    url = f"{BASE_URL}/api/urls"
    headers = {"Authorization": f"Bearer {token}"}
    try:
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            urls = response.json().get("urls", [])
            print("\n--- Tus URLs ---")
            for u in urls:
                print(f"🔗 Original: {u['originalUrl']}")
                short_id= u.get("id") or u.get("shortUrl")
                if short_id:
                    print(f"➡️ Acortada: http://localhost:7070/re/{short_id}")
                else:
                    print("NO se pudo generar la url acortada")
                print(f"📅 Fecha: {u['fecha']}")
                print(f"👁️ Visitas: {u['visitCount']}")
                if u.get("qrCodeBase64"):
                    print("[QR Code: imagen base64]")
                print("-" * 30)
        else:
            print("❌ Error al obtener URLs:", response.text)
    except Exception as e:
        print("🚫 Error:", e)

def create_url(token):
    url = f"{BASE_URL}/api/urls"
    original_url = input("Introduce la URL original: ")
    data = {"originalUrl": original_url}
    headers = {"Authorization": f"Bearer {token}"}

    try:
        response = requests.post(url, headers=headers, data=data)
        if response.ok:
            result = response.json()
            print(" URL creada:")
            print(result)
        else:
            print(" Error al crear URL:", response.text)
    except Exception as e:
        print(" Error:", e)


def main():
    token = None
    while True:
        print("\n=== Menú Cliente REST ===")
        print("1. Iniciar sesión")
        print("2. Listar URLs")
        print("3. Crear URL")
        print("4. Salir")
        opcion = input("Opción: ")

        if opcion == "1":
            token = login()
        elif opcion == "2":
            if token:
                list_urls(token)
            else:
                print("🔒 Inicia sesión primero.")
        elif opcion == "3":
            if token:
               create_url(token)
            else:
                print("Favor iniciar sesion primero!")
        elif opcion == "4":
            break
        else:
            print("Opción inválida.")

if __name__ == "__main__":
    main()
