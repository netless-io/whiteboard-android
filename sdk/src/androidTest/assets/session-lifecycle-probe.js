(function () {
    if (window.__lifecycleProbe) return true;
    if (!window.sdk) return false;
    var probe = window.__lifecycleProbe = { rooms: [], sockets: [], joinStarts: 0, sensorErrors: [], events: [] };
    probe.protocol = [];
    var nativeDispatch = window._handleMessageFromNative;
    window._handleMessageFromNative = function(info) {
        var args = JSON.parse(info.data), marker = args[0] && args[0].__whiteScope;
        probe.protocol.push({at:Date.now(),direction:'native-to-js',method:info.method,scope:marker,argsCount:args.length});
        return nativeDispatch(info);
    };
    var bridgeCall = window.bridge.call.bind(window.bridge);
    window.bridge.call = function(method,value) {
        if (!method.startsWith('_dsb.')) probe.protocol.push({at:Date.now(),direction:'js-to-native',method:method,scope:value && value.scope,valueType:typeof(value && value.value)});
        return bridgeCall(method,value);
    };
    function dereference(entry) { return entry && typeof entry.deref === 'function' ? entry.deref() : entry; }
    function transport(room) {
        var session = room.magix && room.magix.socketSession;
        var connection = session && session.connection;
        return { session: session, connection: connection,
            socket: connection && connection.socket && connection.socket.socket };
    }
    function observe(room, roomId) {
        if (!room) return null;
        var current = transport(room), socket = current.socket;
        if (!socket || typeof socket.readyState !== 'number') {
            if (room.phase === 'connected') probe.sensorErrors.push('Connected Room transport missing');
            return null;
        }
        var known = probe.sockets.find(function(entry) { return dereference(entry.socket) === socket; });
        if (known) return known;
        var serverSession = current.connection.sessionBuild.session;
        var akko=current.connection.socket;
        var entry = { socket: probe.weakRoomReferences ? new WeakRef(socket) : socket, id: probe.sockets.length+1, roomId: roomId,
            session: serverSession, sent: 0, received: 0, heartbeatSent:0, heartbeatReceived:0,
            heartbeatIntervalMs:akko.params.heartbeatInterval,heartbeatTimeoutMs:akko.params.heartbeatTimeout };
        var channel = akko.sessionChannel;
        if (channel && channel.chanel && typeof channel.chanel.sendPackage === 'function') {
            var originalPackage = channel.chanel.sendPackage.bind(channel.chanel);
            channel.chanel.sendPackage = function(type) {
                if (type === 'heartbeat') {entry.heartbeatSent++;entry.lastHeartbeatSentAt=Date.now();}
                return originalPackage.apply(null,arguments);
            };
            var originalReceive = channel.handleReceivedPackage.bind(channel);
            channel.handleReceivedPackage = function(pack) {
                if (pack.type === 'heartbeat') {entry.heartbeatReceived++;entry.lastHeartbeatReceivedAt=Date.now();}
                return originalReceive(pack);
            };
        } else probe.sensorErrors.push('Actual heartbeat channel missing');
        probe.sockets.push(entry);
        probe.events.push({at: Date.now(), type:'transport-observed', roomId:roomId,
            socketId:entry.id, session:serverSession, state:socket.readyState});
        var send = socket.send.bind(socket);
        socket.send = function (data) { entry.sent++; return send(data); };
        socket.addEventListener('message', function () { entry.received++; });
        socket.addEventListener('close', function (event) {
            probe.events.push({at:Date.now(), type:'transport-close', roomId:roomId,
                socketId:entry.id, session:serverSession, code:event.code});
        });
        return entry;
    }
    var originalJoin = window.sdk.joinRoom.bind(window.sdk);
    window.sdk.joinRoom = function (params, callbacks) {
        probe.joinStarts++;
        return originalJoin(params, callbacks).then(function (room) {
            probe.rooms.push(room);
            // Akko caches its WebSocket constructor at module load. Observe the
            // actual transport held by this acquired Room instead of replacing it.
            var roomId = probe.rooms.length;
            observe(room, roomId);
            room.callbacks.on('onPhaseChanged', function(phase) {
                probe.events.push({at:Date.now(), type:'room-phase', roomId:roomId, phase:phase});
            });
            if (probe.holdCreateMs) return new Promise(function(resolve){setTimeout(function(){resolve(room)},probe.holdCreateMs)});
            return room;
        });
    };
    probe.snapshot = function () {
        // Reconnection replaces the transport, while retaining the actual Room object.
        probe.rooms.forEach(function(room, index) { observe(dereference(room),index+1); });
        return {
            at: Date.now(),
            joinStarts: probe.joinStarts,
            protocol: probe.protocol.slice(),
            sensorErrors: probe.sensorErrors.slice(),
            events: probe.events.slice(),
            weakRoomReferences:probe.weakRoomReferences===true,
            rooms: probe.rooms.map(function (record, index) {
                var room=dereference(record);
                if (!room) return {roomId:index+1,phase:'collected'};
                var current = transport(room), socket = current.socket;
                var entry = probe.sockets.find(function(entry) { return dereference(entry.socket) === socket; });
                return {
                    roomId: index+1,
                    session: room.logger && room.logger.context && room.logger.context.session,
                    serverSession: current.session && current.session.session,
                    connectionSession: current.connection && current.connection.sessionBuild.session,
                    socketId: entry && entry.id,
                    observerId: room.observerId,
                    uid: room.uid,
                    phase: room.phase,
                    uuid: room.uuid
                };
            }),
            sockets: probe.sockets.map(function (entry) {
                var socket=dereference(entry.socket);
                return { id:entry.id, roomId:entry.roomId, serverSession:entry.session,
                    state: socket ? socket.readyState : null, socketCollected:!socket, sent: entry.sent, received: entry.received,
                    heartbeatSent:entry.heartbeatSent, heartbeatReceived:entry.heartbeatReceived,
                    heartbeatIntervalMs:entry.heartbeatIntervalMs,
                    heartbeatTimeoutMs:entry.heartbeatTimeoutMs,
                    lastHeartbeatSentAt:entry.lastHeartbeatSentAt,lastHeartbeatReceivedAt:entry.lastHeartbeatReceivedAt };
            })
        };
    };
    probe.disconnectAll = function () {
        return Promise.all(probe.rooms.map(dereference).filter(function(room) {
            return room && room.phase !== 'disconnected';
        }).map(function (room) { return room.disconnect(); }));
    };
    probe.closeTransport = function(index) {
        var socket = transport(dereference(probe.rooms[index])).socket;
        socket.close(4001,'lifecycle transport fault');
        return socket.readyState;
    };
    probe.weakenRoomReferences = function() {
        if (typeof WeakRef !== 'function') throw new Error('WeakRef required for lifecycle retention control');
        probe.rooms=probe.rooms.map(function(room) { return new WeakRef(dereference(room)); });
        probe.sockets.forEach(function(entry) {entry.socket=new WeakRef(dereference(entry.socket));});
        probe.weakRoomReferences=true;
        return true;
    };
    return true;
})()
