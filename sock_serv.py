import socket
import ast, sys

class s_server:
    def __init__(self, ip, port):
        self.HOST = ip #socket.gethostbyname(socket.gethostname())
        self.PORT = int(port)
        self.BUFFER_SIZE = 1024

    def start_server(self):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.bind((self.HOST, self.PORT))
            s.listen()
            connections = []
            print("Main Server starting...")
            while 1:
                    conn, addr = s.accept()
                    
                    data = conn.recv(1024)
                    print(data.decode())

if __name__ == "__main__":
    try:
        ip = sys.argv[1]
        port = sys.argv[2]

    except:
        ip = input("local ip: ")
        port = input("port: ")

    s = s_server(ip,port)
    s.start_server()
