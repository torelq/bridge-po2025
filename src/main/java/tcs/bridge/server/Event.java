package tcs.bridge.server;

import tcs.bridge.communication.messages.ClientToServerMessage;
import tcs.bridge.communication.streams.ServerMessageStream;

import java.net.Socket;

interface Event {}

interface ConnectEvent extends Event {}

record MessageEvent(Client from, ClientToServerMessage message) implements Event {}

record ShutdownEvent() implements Event {}

record TCPConnectEvent(Socket socket) implements ConnectEvent {}

record LocalConnectEvent(ServerMessageStream serverMessageStream) implements ConnectEvent {}

record DisconnectEvent(Client client) implements Event {}