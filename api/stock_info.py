from flask import Flask, request
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy.exc import SQLAlchemyError

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///stocks.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False


db = SQLAlchemy(app)

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column("username", db.String(100), unique=True, nullable=True)
    password = db.Column("password", db.String(100), nullable=True)
    balance = db.Column("balance", db.Float, nullable=False)

class Holding(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    symbol = db.Column("symbol", db.String(100), nullable=False)
    quantity = db.Column("quantity", db.Float, nullable=False)

@app.route("/authenticate_user/<username>/<password>")
def authenticate_user(username, password):
    result = db.session.execute(
        db.select(User).filter(User.username == username, User.password == password)
    ).scalar_one_or_none()
    if not result:
        return {"authenticated": False}, 401

    holdings = db.session.execute(
        db.select(Holding).filter(Holding.user_id == result.id)
    ).scalars().all()

    all_holdings = []
    for holding in holdings:
        all_holdings.append(
            {
                "symbol": holding.symbol,
                "quantity": holding.quantity
            }
        )

    return {
        "authenticated": True,
        "user": {
            "user_id": result.id,
            "username": result.username,
            "balance": result.balance,
            "holdings": all_holdings
        }
    }, 200


@app.route("/logout_user/<int:user_id>", methods=["POST"])
def logout_user(user_id):
    try:
        user = db.session.get(User, user_id)
        if not user:
            return {"success": False, "error": "User not found"}, 404

        payload = request.get_json(silent=True) or {}
        if "balance" in payload:
            user.balance = payload["balance"]

        db.session.execute(db.delete(Holding).where(Holding.user_id == user_id))

        for holding in payload.get("holdings", []):
            symbol = holding.get("symbol")
            quantity = holding.get("quantity", 0)
            if not symbol:
                continue
            db.session.add(Holding(user_id=user_id, symbol=symbol, quantity=quantity))

        db.session.commit()

        return {
            "success": True,
            "user_id": user.id
        }, 200

    except SQLAlchemyError as e:
        db.session.rollback()
        return {"success": False, "error": str(e)}, 500

    finally:
        db.session.remove()

with app.app_context():
    db.create_all()

if __name__ == "__main__":
    app.run()
