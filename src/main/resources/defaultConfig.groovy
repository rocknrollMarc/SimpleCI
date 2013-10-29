web = [
        port: 8080
]

sql = [
    host: "localhost",
    port: "3306",
    username: "root",
    password: "changeme",
    database: "ci"
]

irc = [
        enabled: false,
        host: "irc.esper.net",
        port: 6667,
        nickname: "SimpleCI",
        username: "SimpleCI",
        channels: [
                "#DirectMyFile"
        ]
]