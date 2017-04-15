import socket
serverPort = 12000
serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serverSocket.bind(('', serverPort))
serverSocket.listen(1)
while True:
    connectionSocket, address = serverSocket.accept()
    text = 'Hello from TCP Pythonic Server'.encode()
    connectionSocket.send(text)
    connectionSocket.close()