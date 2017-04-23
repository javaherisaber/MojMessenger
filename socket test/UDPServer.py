import socket
serverPort = 12000
serverSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
serverSocket.bind(('', serverPort))
while True:
    message, clientAddress = serverSocket.recvfrom(2048)
    serverMessage = 'Hey Dude Whats up ?'.encode()
    serverSocket.sendto(serverMessage, clientAddress)
    print(clientAddress)
