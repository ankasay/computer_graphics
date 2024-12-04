
    public class Parallelepiped {
        private final Sides[] sides;

        public Parallelepiped(Sides[] sides) {
            this.sides = sides.clone();
        }

        // Добавляем геттер для sides
        public Sides[] getSides() {
            return sides;
        }

        // Вращение
        // принимает угол и ось вращения,
        // вращает все грани многогранника и возвращает новый объект Parallelepiped с повернутыми гранями.
        public Parallelepiped spin(double rotationAngle, Coordinate3D axis) {
            Sides[] rotatedFaces = new Sides[sides.length];

            for (int i = 0; i < sides.length; i++) {
                rotatedFaces[i] = sides[i].spin(rotationAngle, axis);
            }

            return new Parallelepiped(rotatedFaces);
        }
        public Coordinate[][] ParallelforXY() { // игнорируем координату Z
            var lines = new Coordinate[sides.length * 4][2];
            for (int i = 0; i < sides.length; i++) {
                Edge3D[] edges = sides[i].getEdges();

                for (int j = 0; j < 4; j++) {
                    lines[4 * i + j][0] = new Coordinate(edges[j].getP1().getX(), edges[j].getP1().getY());
                    lines[4 * i + j][1] = new Coordinate(edges[j].getP2().getX(), edges[j].getP2().getY());
                }
            }

            return lines;
        }

        public Coordinate[][] ParallelforXY(Coordinate3D viewDirection){ // проекция с учетом направления наблюдателя
            if (viewDirection == null)
                throw new IllegalArgumentException("Вместо вектора направления наблюдение пришел null");
            if (viewDirection.equals(new Coordinate3D(0, 0, 0)))
                throw new IllegalArgumentException("Вектор направления наблюдения не может быть нулевым");

            var lines = new Coordinate[sides.length * 4][2];
            for (int i = 0; i < sides.length; i++) {
                Edge3D[] edges = sides[i].getEdges();
                Coordinate3D normalVector = sides[i].getNormal();

                if (normalVector.dot(viewDirection) > 0)
                    for (int j = 0; j < 4; j++) {
                        lines[4 * i + j][0] = new Coordinate(edges[j].getP1().getX(), edges[j].getP1().getY());
                        lines[4 * i + j][1] = new Coordinate(edges[j].getP2().getX(), edges[j].getP2().getY());
                    }
                else
                    for (int j = 0; j < 4; j++) {
                        lines[4 * i + j][0] = new Coordinate(-1, -1);
                        lines[4 * i + j][1] = new Coordinate(-1, -1);
                    }
            }

            return lines;
        }

        public Coordinate[][] PerspectiveforYandZ(double k, double q) {
            var lines = new Coordinate[sides.length * 4][2];


            for (int i = 0; i < sides.length; i++) {
                Edge3D[] edges = sides[i].getEdges();

                for (int j = 0; j < 4; j++) {
                    var p1 = edges[j].getP1();
                    var p2 = edges[j].getP2();



                        lines[4 * i + j][0] = new Coordinate(p1.getX(), p1.getY())
                                .multiply(1 / (k * p1.getZ() + q * p1.getY()  + 1));
                        lines[4 * i + j][1] = new Coordinate(p2.getX(), p2.getY())
                                .multiply(1 / (k * p2.getZ() + q * p2.getY() + 1));

                }
            }

            return lines;
        }

        public Coordinate[][] PerspectiveforYandZ(double k, double q, Coordinate3D viewDirection) {
            var lines = new Coordinate[sides.length * 4][2];

            // Матрица перспективного преобразования
            double[][] perspectiveMatrix = {
                    {1, 0, 0, 0},
                    {0, 1, 0, q},
                    {0, 0, 1, k},
                    {0, 0, 0, 1}
            };
            
            for (int i = 0; i < sides.length; i++) {
                Edge3D[] edges = sides[i].getEdges();
                Coordinate3D normalVector = sides[i].modify(perspectiveMatrix).getNormal();

                if (normalVector.dot(viewDirection) > 0) {
                    for (int j = 0; j < 4; j++) {
                        var p1 = edges[j].getP1();
                        var p2 = edges[j].getP2();


                        lines[4 * i + j][0] = new Coordinate(p1.getX(), p1.getY())
                                .multiply(1 / (k * p1.getZ() + q * p1.getY()  + 1));
                        lines[4 * i + j][1] = new Coordinate(p2.getX(), p2.getY())
                                .multiply(1 / (k * p2.getZ() + q * p2.getY() + 1));
                    }
                } else {
                    for (int j = 0; j < 4; j++) {
                        lines[4 * i + j][0] = new Coordinate(-1, -1);
                        lines[4 * i + j][1] = new Coordinate(-1, -1);
                    }
                }
            }

            return lines;
        }

    }