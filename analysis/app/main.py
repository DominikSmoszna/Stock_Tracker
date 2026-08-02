from fastapi import FastAPI
from app.routers import charts, fx, portfolio
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="Chart API",
    version="1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(charts.router)
app.include_router(fx.router)
app.include_router(portfolio.router)

@app.get("/")
async def root():
    return {"message": "Is working"}