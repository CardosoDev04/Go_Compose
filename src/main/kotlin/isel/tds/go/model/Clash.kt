package isel.tds.go.model

import isel.tds.go.storage.Storage

typealias GameStorage = Storage<String, Game>

class Clash (val gs: GameStorage)