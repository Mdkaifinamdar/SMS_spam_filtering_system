import socket
import threading

HOST = '127.0.0.1'
PORT = 1234
LISTENER_LIMIT = 5
active_clients = []  # List of all currently connected users

# Function to listen for upcoming messages from a client
def listen_for_messages(client, username):
    while True:
        try:
            message = client.recv(2048).decode('utf-8')
            if message:
                # Forward message to all clients
                send_messages_to_all(f"{username}~{message}")
            else:
                print(f"The message sent from client {username} is empty")
                break
        except:
            print(f"Error receiving message from {username}")
            break

# Function to send message to a single client
def send_message_to_client(client, message):
    try:
        client.sendall(message.encode())
    except:
        print("Error sending message to a client")

# Function to send any new message to all the clients that
# are currently connected to this server
def send_messages_to_all(message):
    for _, client in active_clients:
        send_message_to_client(client, message)

# Function to handle client
def client_handler(client):
    while True:
        try:
            username = client.recv(2048).decode('utf-8')
            if username:
                active_clients.append((username, client))
                prompt_message = f"SERVER~{username} added to the chat"
                send_messages_to_all(prompt_message)
                threading.Thread(target=listen_for_messages, args=(client, username)).start()
                break
            else:
                print("Client username is empty")
                break
        except:
            print("Error handling client connection")
            break

# Main function
def main():
    # Creating the socket class object
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    try:
        # Provide the server with an address in the form of host IP and port
        server.bind((HOST, PORT))
        print(f"Running the server on {HOST} {PORT}")
    except:
        print(f"Unable to bind to host {HOST} and port {PORT}")
        return

    # Set server limit
    server.listen(LISTENER_LIMIT)

    # This while loop will keep listening to client connections
    while True:
        try:
            client, address = server.accept()
            print(f"Successfully connected to client {address[0]} {address[1]}")
            threading.Thread(target=client_handler, args=(client,)).start()
        except:
            print("Error accepting new client connection")
            break

if __name__ == '__main__':
    main()
