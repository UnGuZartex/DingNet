import sys
from xml.dom import minidom
from dataclasses import dataclass

@dataclass
class Connection:
	id: int
	fromId: int
	toId: int


def containsConnection(conns, src, dst, includeReverse=True):
	for conn in conns:
		if conn.fromId == src and conn.toId == dst:
			return True
		if includeReverse and conn.fromId == dst and conn.toId == src:
		    return True
	return False


def convertFile(fileLocation):
	with open(fileLocation, 'r') as f:
		doc = minidom.parse(f)

	connections = []
	connElement = doc.getElementsByTagName('configuration')[0].getElementsByTagName('connections')[0]
	for conn in connElement.getElementsByTagName('connection'):
		id, src, dst = int(conn.attributes['id'].value), \
			int(conn.attributes['src'].value), \
			int(conn.attributes['dst'].value)

		if not containsConnection(connections, src, dst):
			connections.append(Connection(id, src, dst))


	# for conn in connections:
	# 	print(conn)

	maxId = max(connections, key=lambda x: x.id).id
	# print(maxId)

	for conn in connections:
		maxId+=1

		newEl = doc.createElement('connection')
		newEl.setAttribute('id', str(maxId))
		newEl.setAttribute('src', str(conn.toId))
		newEl.setAttribute('dst', str(conn.fromId))

		connElement.appendChild(newEl)


	with open('bidirectional.xml', 'w') as f:
		# Output is not super pretty, can resave with DingNet if deemed necessary
		f.write(doc.toxml())


def main():
	if len(sys.argv) < 2:
		print('Please make sure to provide the configuration file as an argument')
		return
	convertFile(sys.argv[1])



if __name__ == '__main__':
	main()
