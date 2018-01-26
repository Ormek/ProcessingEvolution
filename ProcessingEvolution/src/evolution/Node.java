package evolution;

import de.my.function.FloatSupplier;

public class Node {
	final static float AIR_FRICTION = Evolution3WEB.AIR_FRICTION;
	final static float GRAVITY = Evolution3WEB.GRAVITY;
	final static float FRICTION = 4;
	final static boolean haveGround = Evolution3WEB.haveGround;
	final static float MUTABILITY_FACTOR = Evolution3WEB.MUTABILITY_FACTOR;
	final float MINIMUM_NODE_SIZE = Evolution3WEB.MINIMUM_NODE_SIZE;
	final float MAXIMUM_NODE_SIZE = Evolution3WEB.MAXIMUM_NODE_SIZE;
	final float MINIMUM_NODE_FRICTION = Evolution3WEB.MINIMUM_NODE_FRICTION;
	final float MAXIMUM_NODE_FRICTION = Evolution3WEB.MAXIMUM_NODE_FRICTION;
	float x, y, vx, vy, m, f;

	Node(float tx, float ty, float tvx, float tvy, float tm, float tf) {
		x = tx;
		y = ty;
		vx = tvx;
		vy = tvy;
		m = tm;
		f = tf;
	}

	/**
	 * Applies Forces to this Node.
	 */
	public void applyForces() {
		vx *= AIR_FRICTION;
		vy *= AIR_FRICTION;
		y += vy;
		x += vx;
	}

	public void applyGravity() {
		vy += GRAVITY;
	}

	public void hitWalls(Iterable<? extends Rectangle> rects) {
		float dif = y + m / 2;
		if (dif >= 0 && haveGround) {
			y = -m / 2;
			vy = 0;
			x -= vx * f;
			if (vx > 0) {
				vx -= f * dif * FRICTION;
				if (vx < 0) {
					vx = 0;
				}
			} else {
				vx += f * dif * FRICTION;
				if (vx > 0) {
					vx = 0;
				}
			}
		}

		for (Rectangle r : rects) {

			boolean flip = false;
			float px, py;
			int section = 0;
			if (Evolution3WEB.abs(x - (r.x1 + r.x2) / 2) <= (r.x2 - r.x1 + m) / 2
					&& Evolution3WEB.abs(y - (r.y1 + r.y2) / 2) <= (r.y2 - r.y1 + m) / 2) {
				if (x >= r.x1 && x < r.x2 && y >= r.y1 && y < r.y2) {
					float d1 = x - r.x1;
					float d2 = r.x2 - x;
					float d3 = y - r.y1;
					float d4 = r.y2 - y;
					if (d1 < d2 && d1 < d3 && d1 < d4) {
						px = r.x1;
						py = y;
						section = 3;
					} else if (d2 < d3 && d2 < d4) {
						px = r.x2;
						py = y;
						section = 5;
					} else if (d3 < d4) {
						px = x;
						py = r.y1;
						section = 1;
					} else {
						px = x;
						py = r.y2;
						section = 7;
					}
					flip = true;
				} else {
					if (x < r.x1) {
						px = r.x1;
						section = 0;
					} else if (x < r.x2) {
						px = x;
						section = 1;
					} else {
						px = r.x2;
						section = 2;
					}
					if (y < r.y1) {
						py = r.y1;
						section += 0;
					} else if (y < r.y2) {
						py = y;
						section += 3;
					} else {
						py = r.y2;
						section += 6;
					}
				}
				float distance = Evolution3WEB.dist(x, y, px, py);
				float rad = m / 2;
				float wallAngle = 0;
				if (distance <= 0.00000001f) { // distance is zero, can't
												// use atan2
					if (section <= 2) {
						wallAngle = Evolution3WEB.PI / 4.0f + section * Evolution3WEB.PI / 4.0f;
					} else if (section >= 6) {
						wallAngle = 5 * Evolution3WEB.PI / 4.0f + (8 - section) * Evolution3WEB.PI / 4.0f;
					} else if (section == 3) {
						wallAngle = Evolution3WEB.PI;
					} else if (section == 5 || section == 4) {
						wallAngle = 0;
					}
					flip = false;
				} else {
					wallAngle = Evolution3WEB.atan2(py - y, px - x);
				}
				if (flip) {
					wallAngle += Evolution3WEB.PI;
				}
				if (distance < rad || flip) {
					dif = rad - distance;
					float multi = rad / distance;
					if (flip) {
						multi = -multi;
					}
					x = (x - px) * multi + px;
					y = (y - py) * multi + py;
					float veloAngle = Evolution3WEB.atan2(vy, vx);
					float veloMag = Evolution3WEB.dist(0, 0, vx, vy);
					float relAngle = veloAngle - wallAngle;
					float relY = Evolution3WEB.sin(relAngle) * veloMag * dif * FRICTION;
					vx = -Evolution3WEB.sin(relAngle) * relY;
					vy = Evolution3WEB.cos(relAngle) * relY;
				}
			}
		}
	}

	public Node copyNode() {
		return (new Node(x, y, 0, 0, m, f));
	}
	
	public Node modifyNode(float mutability, FloatSupplier r) {
		float newX = x + r.getAsFloat() * 0.5f * mutability * MUTABILITY_FACTOR;
		float newY = y + r.getAsFloat() * 0.5f * mutability * MUTABILITY_FACTOR;
		float newM = m + r.getAsFloat() * 0.1f * mutability * MUTABILITY_FACTOR;
		newM = Evolution3WEB.min(Evolution3WEB.max(newM, MINIMUM_NODE_SIZE), MAXIMUM_NODE_SIZE);
		float newF = f + r.getAsFloat() * 0.1f * mutability * MUTABILITY_FACTOR;
		newF = Evolution3WEB.min(Evolution3WEB.max(newF, MINIMUM_NODE_FRICTION), MAXIMUM_NODE_FRICTION);
		return (new Node(newX, newY, 0, 0, newM, newF));// max(m+r()*0.1,0.2),min(max(f+r()*0.1,0),1)
	}

}